package com.lyw.cloudCourse.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudCourse.dto.BatchUpdateResponseDto;
import com.lyw.cloudCourse.dto.CoursesDto;
import com.lyw.cloudCourse.mapper.CoursesDao;
import com.lyw.cloudCourse.service.CoursesBo;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
    @Override
    public CourseResponseWrapper getPopularCourses(Integer limit, String semester) {
        return CourseResponseWrapper.getSuccess(coursesDao.selectPopularCourses(limit,semester,null));
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

            // 1. 验证课程是否存在
            CoursesVo course = coursesDao.selectById(courseId);
            if (ObjectUtil.isEmpty(course)) {
                log.warn("课程不存在: courseId={}", courseId);
                return CourseResponseWrapper.getFailed("课程不存在");
            }

            // 2. 检查课程状态
            if (!"PUBLISHED".equals(course.getStatus())) {
                log.warn("课程状态不可选课: courseId={}, status={}", courseId, course.getStatus());
                return CourseResponseWrapper.getFailed("课程当前不可选课");
            }

            // 3. 检查课程容量
            if (course.getEnrolledCount() >= course.getCapacity()) {
                log.warn("课程容量已满: courseId={}, capacity={}, enrolledCount={}",
                        courseId, course.getCapacity(), course.getEnrolledCount());
                return CourseResponseWrapper.getFailed("课程容量已满");
            }

            // 4. 更新选课人数
            int newEnrolledCount = course.getEnrolledCount() + 1;
            course.setEnrolledCount(newEnrolledCount);
            int updateResult = coursesDao.updateById(course);

            if (updateResult <= 0) {
                log.error("更新课程选课人数失败: courseId={}", courseId);
                return CourseResponseWrapper.getFailed("更新选课人数失败");
            }

            log.info("成功增加课程选课人数: courseId={}, 新人数={}", courseId, newEnrolledCount);

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

            // 1. 验证课程是否存在
            CoursesVo course = coursesDao.selectById(courseId);
            if (course == null) {
                log.warn("课程不存在: courseId={}", courseId);
                return CourseResponseWrapper.getFailed("课程不存在");
            }

            // 2. 检查当前选课人数
            if (course.getEnrolledCount() <= 0) {
                log.warn("课程选课人数已为0: courseId={}", courseId);
                return CourseResponseWrapper.getFailed("课程选课人数已为0，无法减少");
            }

            // 3. 更新选课人数
            int newEnrolledCount = course.getEnrolledCount() - 1;
            course.setEnrolledCount(newEnrolledCount);
            int updateResult = coursesDao.updateById(course);

            if (updateResult <= 0) {
                log.error("更新课程选课人数失败: courseId={}", courseId);
                return CourseResponseWrapper.getFailed("更新选课人数失败");
            }

            log.info("成功减少课程选课人数: courseId={}, 新人数={}", courseId, newEnrolledCount);

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
}