package com.lyw.cloudChoose.strategy.drop.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.service.EnrollmentService;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component("IMMEDIATE_DROP")
public class ImmediateDropStrategy implements DropStrategy {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private DefaultRedisScript<Long> dropScript;
    @Resource
    private EnrollmentsDao enrollmentsDao;
    @Resource
    private EnrollmentService enrollmentService;

    @Override
    public CourseResponseWrapper drop(EnrollmentsVo enrollment, CoursesDto course,
                                      EnrollmentStrategiesVo strategy) {

        Long courseId = enrollment.getCourseId();
        Long studentId = enrollment.getStudentId();

        log.info("开始退课处理 - 学生ID: {}, 课程ID: {}", studentId, courseId);

        // 使用Lua脚本进行原子性退课操作
        List<String> keys = Arrays.asList(
                RedisKeyConstant.COURSE_CURRENT + courseId,
                RedisKeyConstant.STUDENT_COURSES + studentId
        );

        List<String> args = Arrays.asList(
                courseId.toString(),
                String.valueOf(RedisKeyConstant.DEFAULT_EXPIRE_SECONDS)
        );

        Long dropResult = stringRedisTemplate.execute(dropScript, keys, args.toArray());
        if (ObjectUtil.isEmpty(dropResult) || dropResult == 2L) {
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        }
        return handleDropResult(dropResult, enrollment, course, studentId, courseId);
    }
    /**
     * 根据选课结果处理不同的情况
     */
    private CourseResponseWrapper handleDropResult(Long dropResult, EnrollmentsVo enrollment,
                                                     CoursesDto course, Long studentId, Long courseId) {
        switch (dropResult.intValue()) {
            case 1:
                log.warn("Redis中未找到选课记录，需要检查数据库 - 学生ID: {}, 课程ID: {}", studentId, courseId);
                return handleDatabaseCleanup(enrollment, course, studentId);
            case 0:
                return enrollmentService.handleSuccessfulDrop(enrollment, course, studentId);

            default:
                return CourseResponseWrapper.getFailed("未知错误");
        }
    }
    /**
     * 处理数据库清理（当Redis中无记录但数据库有记录时）
     */
    private CourseResponseWrapper handleDatabaseCleanup(EnrollmentsVo enrollment, CoursesDto course, Long studentId) {
        try {
            // 检查数据库记录是否存在
            EnrollmentsVo dbEnrollment = enrollmentsDao.selectById(enrollment.getId());
            if (dbEnrollment != null) {
                // 数据库记录存在，执行退课流程
                return enrollmentService.handleSuccessfulDrop(dbEnrollment, course, studentId);
            } else {
                log.info("数据库中也无选课记录，无需处理 - enrollmentId: {}", enrollment.getId());
                return CourseResponseWrapper.getSuccess("退选成功", enrollment);
            }
        } catch (Exception e) {
            log.error("数据库清理检查失败 - enrollmentId: {}", enrollment.getId(), e);
            return CourseResponseWrapper.getFailed("系统异常，请稍后重试");
        }
    }
}