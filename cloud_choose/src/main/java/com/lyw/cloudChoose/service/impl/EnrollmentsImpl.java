package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.dto.StudentProfileDto;
import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.factory.DropStrategyFactory;
import com.lyw.cloudChoose.factory.EnrollmentStrategyFactory;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.feign.MemberFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentStrategiesDao;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentValidationService;
import com.lyw.cloudChoose.service.EnrollmentsBo;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EnrollmentsImpl extends ServiceImpl<EnrollmentsDao, EnrollmentsVo> implements EnrollmentsBo {

    @Resource
    private EnrollmentsDao enrollmentsDao;

    @Resource
    private EnrollmentStrategiesDao enrollmentStrategiesDao;

    @Resource
    private CourseFeignService courseFeignService;

    @Resource
    private EnrollmentStrategyFactory strategyFactory;
    @Resource
    private EnrollmentValidationService enrollmentValidationService;
    @Resource
    private DropStrategyFactory dropStrategyFactory;
    @Resource
    private Executor enrollmentExecutor;
    @Resource
    private DistributedCacheHelper distributedCacheHelper;
    @Resource
    private MemberFeignService memberFeignService;

    private EnrollmentStrategiesVo createDefaultStrategy(Long courseId) {
        // 创建默认的先到先得策略
        EnrollmentStrategiesVo enrollmentStrategiesVo = new EnrollmentStrategiesVo();
        enrollmentStrategiesVo.setCourseId(courseId);
        enrollmentStrategiesVo.setStrategyType("FIRST_COME");
        enrollmentStrategiesVo.setDropStrategyType("IMMEDIATE_DROP");
        enrollmentStrategiesVo.setAutoWaitlist(true);
        enrollmentStrategiesVo.setAllowAudit(true);
        enrollmentStrategiesVo.setConflictCheck(true);
        enrollmentStrategiesVo.setPrerequisiteCheck(false);
        return enrollmentStrategiesVo;
    }

    // 课程信息缓存
    private CoursesDto getCachedCourse(Long courseId) {
        return FeignResponseHelper.convert(courseFeignService.searchDetail(courseId), CoursesDto.class);
    }

    // 策略信息缓存
    private EnrollmentStrategiesVo getCachedStrategy(Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.ENROLLMENT_STRATEGY,
                        RedisKeyConstant.LOCK_ENROLLMENT_STRATEGY,
                        Duration.ofHours(24)
                ),
                () -> {
                    EnrollmentStrategiesVo strategy = enrollmentStrategiesDao.selectOne(
                            new LambdaQueryWrapper<EnrollmentStrategiesVo>()
                                    .eq(EnrollmentStrategiesVo::getCourseId, courseId));
                    return ObjectUtil.isNotEmpty(strategy) ? strategy : createDefaultStrategy(courseId);
                },
                EnrollmentStrategiesVo.class
        );
    }

    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto dto) {
        Long courseId = dto.getCourseId();
        Long studentId = dto.getStudentId();
        try {
            log.info("开始选课流程: studentId={}, courseId={}", studentId, courseId);

            // 并行获取课程、策略信息、黑名单信息
            CompletableFuture<CoursesDto> courseFuture = CompletableFuture.supplyAsync(
                    () -> getCachedCourse(courseId), enrollmentExecutor);

            CompletableFuture<EnrollmentStrategiesVo> strategyFuture = CompletableFuture.supplyAsync(
                    () -> getCachedStrategy(courseId), enrollmentExecutor);

            // 等待并行任务完成
            CoursesDto course = courseFuture.get(2, TimeUnit.SECONDS);
            EnrollmentStrategiesVo strategy = strategyFuture.get(2, TimeUnit.SECONDS);

            // 完整验证
            ValidationResult validationResult = enrollmentValidationService.validateEnrollment(dto, course, strategy);

            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略类型执行选课逻辑
            EnrollmentStrategy enrollmentStrategy = strategyFactory.getStrategy(strategy.getStrategyType());
            return enrollmentStrategy.enroll(dto, course, strategy);

        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", studentId, courseId);
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    public CourseResponseWrapper drop(Long enrollmentId, EnrollmentsDto dto) {
        // 获取选课记录
        EnrollmentsVo enrollment = enrollmentsDao.selectById(enrollmentId);
        if (ObjectUtil.isEmpty(enrollment)) {
            return CourseResponseWrapper.getFailed("选课记录不存在");
        }

        // 权限验证
        if (!enrollment.getStudentId().equals(dto.getStudentId())) {
            return CourseResponseWrapper.getFailed("无权操作此选课记录");
        }

        // 获取课程信息和策略
        CompletableFuture<CoursesDto> courseFuture = CompletableFuture.supplyAsync(
                () -> getCachedCourse(enrollment.getCourseId()),enrollmentExecutor);

        CompletableFuture<EnrollmentStrategiesVo> strategyFuture = CompletableFuture.supplyAsync(
                () -> getCachedStrategy(enrollment.getCourseId()),enrollmentExecutor);

        try {

            // 等待并行任务完成
            CoursesDto course = courseFuture.get(2, TimeUnit.SECONDS);
            EnrollmentStrategiesVo strategy = strategyFuture.get(2, TimeUnit.SECONDS);

            // 并行进行完整退选验证
            ValidationResult validationResult = enrollmentValidationService.validateDrop(dto, course, strategy);
            if (!validationResult.isValid()) {
                String rejectionReasons = String.join("; ", validationResult.getRejectionReasons());
                return CourseResponseWrapper.getFailed(rejectionReasons);
            }

            // 根据策略执行退选
            DropStrategy dropStrategy = dropStrategyFactory.getStrategy(strategy.getDropStrategyType());
            return dropStrategy.drop(enrollment, course, strategy);

        } catch (TimeoutException e) {
            log.warn("选课操作超时: studentId={}, courseId={}", dto.getStudentId(), dto.getCourseId());
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        } catch (Exception e) {
            log.error("并行处理异常", e);
            return CourseResponseWrapper.getFailed("系统异常");
        }
    }

    @Override
    public CourseResponseWrapper getStudentEnrollments(Long studentId, EnrollmentsDto dto) {
        try {
            // 查询学生选课记录
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getStudentId, studentId));

            if (CollectionUtil.isEmpty(enrollments)) {
                return CourseResponseWrapper.getSuccess("查询成功，暂无选课记录", Collections.emptyList());
            }

            // 提取课程ID并去重
            List<Long> courseIds = enrollments.stream()
                    .map(EnrollmentsVo::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量查询课程信息（避免N+1查询）
            List<CoursesDto> courseBasicInfoDtos = FeignResponseHelper.convertToList(
                    courseFeignService.searchBatchByIds(courseIds), CoursesDto.class);

            if (CollectionUtil.isEmpty(courseBasicInfoDtos)) {
                // 如果没有课程信息，返回空课程数据
                enrollments.forEach(e -> e.setCourses(null));
                return CourseResponseWrapper.getSuccess("查询成功，但部分课程信息缺失", enrollments);
            }

            // 使用Map优化查询效率：O(1)查找代替O(n^2)嵌套循环
            Map<Long, CoursesDto> courseMap = courseBasicInfoDtos.stream()
                    .collect(Collectors.toMap(CoursesDto::getId, Function.identity(), (v1, v2) -> v1));

            // 设置课程信息到选课记录中
            enrollments.forEach(enrollment -> {
                CoursesDto courseDto = courseMap.get(enrollment.getCourseId());
                enrollment.setCourses(courseDto);
            });

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询学生选课列表异常: studentId={}, dto={}", studentId, dto, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    public CourseResponseWrapper getCourseEnrollments(Long courseId, EnrollmentsDto dto) {
        try {
            List<EnrollmentsVo> enrollments = enrollmentsDao.selectList(
                    new LambdaQueryWrapper<EnrollmentsVo>()
                            .eq(EnrollmentsVo::getCourseId, courseId));
            List<Long> studentIds = enrollments.
                    stream()
                    .map(item -> item.getStudentId())
                    .distinct()
                    .collect(Collectors.toList());

            List<StudentProfileDto> studentBasicInfoDtos = FeignResponseHelper.convertToList(
                    memberFeignService.searchBatchByIds(studentIds), StudentProfileDto.class);

            if (CollectionUtil.isEmpty(studentBasicInfoDtos)) {
                enrollments.forEach(e -> e.setStudentProfile(null));
                return CourseResponseWrapper.getSuccess("查询成功，但部分学生信息缺失", enrollments);
            }

            Map<Long, StudentProfileDto> studentMap = studentBasicInfoDtos.stream()
                    .collect(Collectors.toMap(StudentProfileDto::getId, Function.identity(), (v1, v2) -> v1));

            enrollments.forEach(enrollment -> {
                StudentProfileDto studentProfileDto = studentMap.get(enrollment.getCourseId());
                enrollment.setStudentProfile(studentProfileDto);
            });

            return CourseResponseWrapper.getSuccess("查询成功", enrollments);
        } catch (Exception e) {
            log.error("查询课程选课列表异常: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
}