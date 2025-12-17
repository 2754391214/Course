package com.lyw.cloudCourse.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudCourse.dto.BatchUpdateResponseDto;
import com.lyw.cloudCourse.dto.CoursesDto;
import com.lyw.cloudCourse.dto.TeacherProfileDto;
import com.lyw.cloudCourse.dto.UserDto;
import com.lyw.cloudCourse.feign.InteractionFeignService;
import com.lyw.cloudCourse.feign.MemberFeignService;
import com.lyw.cloudCourse.mapper.CoursesDao;
import com.lyw.cloudCourse.service.CoursesBo;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 课程基础信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Service
@Slf4j
public class CoursesImpl extends BaseImpl<CoursesDao, CoursesVo, CoursesDto> implements CoursesBo {
    @Resource
    private CoursesDao coursesDao;
    @Resource
    private InteractionFeignService interactionFeignService;
    @Resource
    private MemberFeignService memberFeignService;
    @Resource
    private DistributedCacheHelper distributedCacheHelper;
    private CoursesVo getCachedCourse(Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.COURSE_INFO,
                        RedisKeyConstant.LOCK_COURSE_INFO,
                        Duration.ofHours(24)
                ),
                () -> {
                    CoursesVo coursesVo = baseMapper.selectAllDataById(courseId);
                    if (ObjectUtil.isEmpty(coursesVo)) {
                        return null;
                    }
                    Long userId = Long.valueOf(CurUserUtil.getUserId());
                    if (ObjectUtil.isEmpty(userId)) {
                        // 未登录用户
                        coursesVo.setIsFavorited(false);
                        coursesVo.setIsLiked(false);
                        coursesVo.setFavoritedCount(0);
                        coursesVo.setLikedCount(0);
                    } else {
                        try {
                            Map<String, Object> status = FeignResponseHelper.convertSafe(interactionFeignService.getFavoriteStatus(CommonKeyConstant.COURSE, courseId, userId), Map.class);
                            coursesVo.setIsFavorited(getBoolean(status.get("isFavorited"), false));
                            coursesVo.setFavoritedCount(getInteger(status.get("favoritedCount"), 0));
                            coursesVo.setIsLiked(getBoolean(status.get("isLiked"), false));
                            coursesVo.setLikedCount(getInteger(status.get("likedCount"), 0));
                            TeacherProfileDto teacherProfileDto = FeignResponseHelper.convertSafe(memberFeignService.searchDetail(coursesVo.getTeacherId()), TeacherProfileDto.class);
                            if(ObjectUtil.isNotEmpty(teacherProfileDto)){
                                coursesVo.setTeacherName(teacherProfileDto.getName());
                                coursesVo.setTeacherAvatat(teacherProfileDto.getAvatar());
                            }
                        }catch (Exception e) {
                            log.error("远程异常:{}", e);
                        }
                    }
                    return coursesVo;
                },
                CoursesVo.class
        );
    }
    @Override
    public CourseResponseWrapper findById(Long id) {
        if (ObjectUtil.isEmpty(id)) {
            return CourseResponseWrapper.getFailed("参数错误");
        }
        return CourseResponseWrapper.getSuccess(getCachedCourse(id));
    }

    // 工具方法
    private Boolean getBoolean(Object value, Boolean defaultValue) {
        return value instanceof Boolean ? (Boolean) value : defaultValue;
    }

    private Integer getInteger(Object value, Integer defaultValue) {
        return value instanceof Number ? ((Number) value).intValue() : defaultValue;
    }
    @Override
    public CourseResponseWrapper getTeacherCourses(Long teacherId, String semester) {
        return CourseResponseWrapper.getSuccess(coursesDao.selectList(new LambdaQueryWrapper<CoursesVo>().eq(CoursesVo::getTeacherId,teacherId).eq(CoursesVo::getSemester,semester)));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper incrementEnrollment(Long courseId) {
        try {
            log.info("开始增加课程选课人数: courseId={}", courseId);

            int updateResult = coursesDao.updateEnrolledCountAtomically(courseId);
            if (updateResult <= 0) {
                log.warn("增加选课人数失败，可能课程不存在、已满或状态不可用: courseId={}", courseId);

                // 获取最新课程信息以确定具体失败原因
                CoursesVo currentCourse = coursesDao.selectById(courseId);
                if (ObjectUtil.isEmpty(currentCourse)) {
                    return CourseResponseWrapper.getFailed("课程不存在");
                }
                if (!"PUBLISHED".equals(currentCourse.getStatus())) {
                    return CourseResponseWrapper.getFailed("课程当前不可选课");
                }
                if (currentCourse.getEnrolledCount() >= currentCourse.getCapacity()) {
                    return CourseResponseWrapper.getFailed("课程容量已满");
                }
                return CourseResponseWrapper.getFailed("选课失败，请重试");
            }
            log.info("成功增加课程选课人数: courseId={}", courseId);

            // 返回更新后的课程信息
            CoursesVo updatedCourse = coursesDao.selectById(courseId);
            return CourseResponseWrapper.getSuccess("增加选课人数成功", updatedCourse);

        } catch (Exception e) {
            log.error("增加课程选课人数异常: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper decrementEnrollment(Long courseId) {
        try {
            log.info("开始减少课程选课人数: courseId={}", courseId);

            int updateResult = coursesDao.decrementEnrolledCountAtomically(courseId);

            if (updateResult <= 0) {
                log.warn("减少选课人数失败: courseId={}", courseId);

                CoursesVo currentCourse = coursesDao.selectById(courseId);
                if (currentCourse == null) {
                    return CourseResponseWrapper.getFailed("课程不存在");
                }
                if (currentCourse.getEnrolledCount() <= 0) {
                    return CourseResponseWrapper.getFailed("课程选课人数已为0，无法减少");
                }
                return CourseResponseWrapper.getFailed("减少选课人数失败");
            }

            log.info("成功减少课程选课人数: courseId={}", courseId);

            // 返回更新后的课程信息
            CoursesVo updatedCourse = coursesDao.selectById(courseId);
            return CourseResponseWrapper.getSuccess("减少选课人数成功", updatedCourse);

        } catch (Exception e) {
            log.error("减少课程选课人数异常: courseId={}", courseId, e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper batchUpdateCapacity(List<CoursesDto> updates) {
        try {
            log.info("开始批量更新课程容量: 更新数量={}", updates.size());
            if (CollectionUtil.isEmpty(updates)) {
                return CourseResponseWrapper.getFailed("更新数据不能为空");
            }
            int successCount = 0;
            int failureCount = 0;
            List<CoursesVo> results = new ArrayList<>();
            for (CoursesDto update : updates) {
                CoursesVo result = new CoursesVo();
                result.setId(update.getId());
                result.setNewCurrentEnrollment(update.getNewCurrentEnrollment());

                try {
                    // 1. 验证课程是否存在
                    CoursesVo course = coursesDao.selectById(update.getId());
                    if (course == null) {
                        result.setSuccess(false);
                        result.setMessage("课程不存在");
                        failureCount++;
                        results.add(result);
                        continue;
                    }

                    // 2. 验证新的选课人数是否合理
                    if (update.getNewCurrentEnrollment() < 0) {
                        result.setSuccess(false);
                        result.setMessage("选课人数不能为负数");
                        failureCount++;
                        results.add(result);
                        continue;
                    }

                    if (update.getNewCurrentEnrollment() > course.getCapacity()) {
                        result.setSuccess(false);
                        result.setMessage("选课人数超过课程容量");
                        failureCount++;
                        results.add(result);
                        continue;
                    }

                    // 3. 更新课程选课人数
                    course.setEnrolledCount(update.getNewCurrentEnrollment());
                    int updateResult = coursesDao.updateById(course);

                    if (updateResult > 0) {

                        result.setSuccess(true);
                        result.setMessage("更新成功");
                        successCount++;
                        log.info("批量更新课程容量成功: courseId={}, newEnrollment={}",
                                update.getId(), update.getNewCurrentEnrollment());
                    } else {
                        result.setSuccess(false);
                        result.setMessage("更新数据库失败");
                        failureCount++;
                    }

                } catch (Exception e) {
                    log.error("批量更新单个课程异常: courseId={}", update.getId(), e);
                    result.setSuccess(false);
                    result.setMessage("系统异常: " + e.getMessage());
                    failureCount++;
                }

                results.add(result);
            }

            // 构建批量更新结果
            BatchUpdateResponseDto response = new BatchUpdateResponseDto();
            response.setTotalCount(updates.size());
            response.setSuccessCount(successCount);
            response.setFailureCount(failureCount);
            response.setResults(results);

            String message = String.format("批量更新完成: 成功%d个, 失败%d个", successCount, failureCount);
            log.info("批量更新课程容量完成: {}", message);

            return CourseResponseWrapper.getSuccess(message, response);

        } catch (Exception e) {
            log.error("批量更新课程容量异常", e);
            return CourseResponseWrapper.getFailed("批量更新失败: " + e.getMessage());
        }
    }

    @Override
    public CourseResponseWrapper searchBatchByIds(List<Long> courseIds) {
        return CourseResponseWrapper.getSuccess(baseMapper.selectBatchIds(courseIds));
    }
}