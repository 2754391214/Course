package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.dto.CourseSchedulesDto;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentBlacklistDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentValidationService;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import com.lyw.commonUtil.util.reidsCache.setCache.DistributedSetCacheHelper;
import com.lyw.commonUtil.util.reidsCache.setCache.SetCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrollmentValidationServiceImpl implements EnrollmentValidationService {

    @Resource
    private EnrollmentBlacklistDao enrollmentBlacklistDao;
    @Resource
    private EnrollmentsDao enrollmentsDao;
    @Resource
    private CourseFeignService courseFeignService;
    @Resource
    private Executor validationExecutor;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private DistributedCacheHelper distributedCacheHelper;
    @Resource
    private DistributedSetCacheHelper distributedSetCacheHelper;
    @Override
    public ValidationResult validateEnrollment(EnrollmentsDto request, CoursesDto course,
                                               EnrollmentStrategiesVo strategy) {
        ValidationResult result = ValidationResult.success();

        // 1. 必须最先执行
        if (ObjectUtil.isEmpty(course)) {
            result.addRejectionReason("课程不存在");
            return result;
        }

        // 2. 并行执行其他验证
        CompletableFuture<Boolean> basicInfoFuture = CompletableFuture.supplyAsync(
                () -> validateBasicInfo(request, course, strategy, result),validationExecutor);

        CompletableFuture<Boolean> blacklistFuture = CompletableFuture.supplyAsync(
                () -> validateBlacklist(request, result),validationExecutor);

        CompletableFuture<Boolean> timeConflictFuture = CompletableFuture.supplyAsync(
                () -> validateTimeConflict(request, course, strategy, result),validationExecutor);

        // 等待所有验证完成
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    basicInfoFuture, blacklistFuture, timeConflictFuture);
            allFutures.get(2, TimeUnit.SECONDS);

            // 如果有任何一个验证失败，直接返回
            if (!result.isValid()) {
                return result;
            }

        } catch (TimeoutException e) {
            log.warn("验证操作超时");
            result.addRejectionReason("系统繁忙，请稍后重试");
            return result;
        } catch (Exception e) {
            log.error("并行验证异常", e);
            result.addRejectionReason("系统异常");
            return result;
        }

        return result;
    }

    //基本信息校验
    private boolean validateBasicInfo(EnrollmentsDto request, CoursesDto course,
                                      EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("执行基础信息验证");

        if (!"PUBLISHED".equals(course.getStatus())) {
            result.addRejectionReason("课程未发布或已关闭");
            return false;
        }

        if (!Arrays.asList("NORMAL", "AUDIT").contains(request.getEnrollmentType())) {
            result.addRejectionReason("无效的选课类型");
            return false;
        }

        if ("AUDIT".equals(request.getEnrollmentType()) &&
                !Boolean.TRUE.equals(strategy.getAllowAudit())) {
            result.addRejectionReason("该课程不允许旁听");
            return false;
        }

        return true;
    }


    // 选课黑名单信息缓存
    private EnrollmentBlacklistVo getEnrollmentBlack(Long studentId,Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.ENROLLMENT_BLACK,
                        RedisKeyConstant.LOCK_ENROLLMENT_BLACK,
                        Duration.ofHours(24)
                ),
                () -> {
                    return enrollmentBlacklistDao.selectByStudentId(studentId, courseId);
                },
                EnrollmentBlacklistVo.class
        );
    }
    //选课校验
    private boolean validateBlacklist(EnrollmentsDto request, ValidationResult result) {
        log.info("检查黑名单");

        EnrollmentBlacklistVo blacklist = getEnrollmentBlack(request.getStudentId(), request.getCourseId());
        if (ObjectUtil.isNotEmpty(blacklist)) {
            String reason = ObjectUtil.isEmpty(blacklist.getCourseId()) ?
                    String.format("学生处于黑名单中，禁止选课。原因: %s", blacklist.getReason()) :
                    String.format("该课程对您受限，禁止选课。原因: %s", blacklist.getReason());

            result.addRejectionReason(reason);
            return false;
        }

        log.info("检查黑名单 完成");
        return true;
    }


    /**
     * 批量获取课程时间表
     */
    public List<CourseSchedulesDto> getCourseSchedules(List<Long> courseIds) {
        List<CourseSchedulesDto> result = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        // 批量从Redis获取
        for (Long courseId : courseIds) {
            String key = RedisKeyConstant.COURSE_SCHEDULES+courseId;
            try {
                List<CourseSchedulesDto> schedules = redisUtils.get(key);
                if (CollectionUtil.isNotEmpty(schedules)) {
                    result.addAll(schedules);
                } else {
                    missingIds.add(courseId);
                }
            } catch (Exception e) {
                log.error("获取课程时间表缓存失败: courseId={}", courseId, e);
                missingIds.add(courseId);
            }
        }

        // 如果有缺失，从课程服务获取并更新缓存
        if (!missingIds.isEmpty()) {
            List<CourseSchedulesDto> remoteSchedules = fetchSchedulesFromRemote(missingIds);
            result.addAll(remoteSchedules);
            updateCourseSchedulesCache(remoteSchedules);
        }

        return result;
    }
    private List<CourseSchedulesDto> fetchSchedulesFromRemote(List<Long> courseIds) {
        try {
            // 调用课程服务的批量接口
            List<CourseSchedulesDto> convert = FeignResponseHelper.convertToList(courseFeignService.batchGetCourseSchedules(courseIds), CourseSchedulesDto.class);
            if (CollectionUtil.isNotEmpty(convert)) {
                return convert;
            }
        } catch (Exception e) {
            log.error("从课程服务获取时间表失败: courseIds={}", courseIds, e);
        }
        return Collections.emptyList();
    }
    private void updateCourseSchedulesCache(List<CourseSchedulesDto> schedules) {
        if (CollectionUtil.isNotEmpty(schedules)){
            Map<Long, List<CourseSchedulesDto>> listMap = schedules.stream().collect(Collectors.groupingBy(CourseSchedulesDto::getCourseId));
            for (Map.Entry<Long, List<CourseSchedulesDto>> entry : listMap.entrySet()) {
                Long courseId = entry.getKey();
                String key = RedisKeyConstant.COURSE_SCHEDULES+courseId;
                try {
                    redisUtils.set(key, entry.getValue(),Duration.ofHours(24));
                } catch (Exception e) {
                    log.error("更新课程时间表缓存失败: courseId={}", courseId, e);
                }
            }
        }
    }
    /**
     * 检查时间安排冲突
     */
    private Boolean checkScheduleConflicts(List<CourseSchedulesDto> targetSchedules,
                                           List<CourseSchedulesDto> studentSchedules) {
        // 按星期几分组，提高比较效率
        Map<String, List<CourseSchedulesDto>> targetSchedulesByDay = targetSchedules.stream()
                .collect(Collectors.groupingBy(CourseSchedulesDto::getDayOfWeek));

        Map<String, List<CourseSchedulesDto>> studentSchedulesByDay = studentSchedules.stream()
                .collect(Collectors.groupingBy(CourseSchedulesDto::getDayOfWeek));

        // 只检查有相同星期几的课程
        for (String dayOfWeek : targetSchedulesByDay.keySet()) {
            if (!studentSchedulesByDay.containsKey(dayOfWeek)) {
                continue;
            }

            List<CourseSchedulesDto> targetDaySchedules = targetSchedulesByDay.get(dayOfWeek);
            List<CourseSchedulesDto> studentDaySchedules = studentSchedulesByDay.get(dayOfWeek);

            // 检查同一天的时间冲突
            for (CourseSchedulesDto targetSchedule : targetDaySchedules) {
                for (CourseSchedulesDto studentSchedule : studentDaySchedules) {
                    if (isTimeConflict(targetSchedule, studentSchedule)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 检查两个时间安排是否冲突
     */
    private boolean isTimeConflict(CourseSchedulesDto schedule1, CourseSchedulesDto schedule2) {
        if (ObjectUtil.isEmpty(schedule1.getStartTime()) || ObjectUtil.isEmpty(schedule1.getEndTime()) ||
                ObjectUtil.isEmpty(schedule2.getStartTime()) || ObjectUtil.isEmpty(schedule2.getEndTime())) {
            return false;
        }
        // 时间冲突判断：两个时间段有重叠
        return schedule1.getStartTime().isBefore(schedule2.getEndTime()) &&
                schedule1.getEndTime().isAfter(schedule2.getStartTime());
    }
    /**
     * 获取学生所选课程ID列表
     */
    public Set<Long> getStudentCourseIds(Long studentId) {
        return distributedSetCacheHelper.getOrLoad(
                String.valueOf(studentId), // key后缀：学生ID
                new SetCacheConfig()
                        .setCacheKeyPrefix(RedisKeyConstant.STUDENT_COURSES)
                        .setLockKeyPrefix(RedisKeyConstant.LOCK_STUDENT_COURSES)
                        .setCacheTimeout(30 * 60 * 1000L) // 30分钟
                        .setNullValueTimeout(5 * 60 * 1000L) // 5分钟
                        .setLockWaitTime(3000L)
                        .setLockLeaseTime(10000L),
                () -> {
                    // 数据加载逻辑 - 从数据库查询并转换为Set
                    List<Long> courseList = enrollmentsDao.selectEnrolledCourseIds(studentId);
                    return new HashSet<>(courseList);
                },
                Long.class
        );
    }
    //课程时间冲突校验
    private boolean validateTimeConflict(EnrollmentsDto request, CoursesDto course,
                                         EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("检查时间冲突");
        long totalStartTime = System.currentTimeMillis();
        if (!Boolean.TRUE.equals(strategy.getConflictCheck())) {
            return true;
        }

        try {
            Set<Long> enrolledCourseIds = getStudentCourseIds(request.getStudentId());
            if (enrolledCourseIds.isEmpty()) {
                log.info("学生未选任何课程，无需检查时间冲突");
                return true;
            }
            List<CourseSchedulesDto> studentSchedules = getCourseSchedules(new ArrayList<>(enrolledCourseIds));
            List<CourseSchedulesDto> currentCourseSchedules = course.getCourseSchedules();
            if (CollectionUtil.isEmpty(currentCourseSchedules)) {
                log.info("当前课程无时间安排，跳过冲突检查");
                return true;
            }
            if (checkScheduleConflicts(currentCourseSchedules, studentSchedules)) {
                result.addRejectionReason("该课程与其他已选课程存在时间冲突");
                return false;
            }
            log.info("检查时间冲突 完成, 耗时: {}ms", System.currentTimeMillis() - totalStartTime);
            return true;
        } catch (Exception e) {
            log.error("时间冲突检查异常", e);
            result.addRejectionReason("时间冲突检查服务异常，请稍后重试");
            return false;
        }
    }


    @Override
    public ValidationResult validateDrop(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        ValidationResult result = ValidationResult.success();
        // 1. 基础验证
        if (!validateBasicInfoDrop(request, course, strategy, result)&&!validateCheckTime(request, result)) {
            return result;
        }

        return result;
    }

    //退课校验
    private boolean validateBasicInfoDrop(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationResult result) {
        log.info("执行基础信息验证");

        if (ObjectUtil.isEmpty(course)) {
            result.addRejectionReason("课程不存在");
            return false;
        }

        if (!"PUBLISHED".equals(course.getStatus())) {
            result.addRejectionReason("课程未发布或已关闭");
            return false;
        }

        return true;
    }
    //TODO
    private boolean validateCheckTime(EnrollmentsDto request, ValidationResult result) {
        log.info("检查课程是否开始");
        return true;
    }
}
