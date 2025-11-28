package com.lyw.cloudChoose.strategy.enrollment.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.service.EnrollmentService;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 先到先得策略
 */
@Slf4j
@Component("FIRST_COME")
public class FirstComeFirstServedStrategy implements EnrollmentStrategy {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DefaultRedisScript<Long> enrollScript;

    @Resource
    private EnrollmentService enrollmentService;
    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        Long courseId = course.getId();
        Long studentId = request.getStudentId();

        List<String> keys = Arrays.asList(
                RedisKeyConstant.COURSE_CURRENT + courseId,
                RedisKeyConstant.STUDENT_COURSES + studentId
        );

        List<String> args = Arrays.asList(
                course.getCapacity().toString(),
                courseId.toString(),
                String.valueOf(RedisKeyConstant.DEFAULT_EXPIRE_SECONDS)
        );

        Long enrollResult = stringRedisTemplate.execute(enrollScript, keys, args.toArray());

        if (ObjectUtil.isEmpty(enrollResult) || enrollResult == 3L) {
            return CourseResponseWrapper.getFailed("系统繁忙，请稍后重试");
        }

        return handleEnrollResult(enrollResult, request, course, studentId);
    }

    /**
     * 根据选课结果处理不同的情况
     */
    private CourseResponseWrapper handleEnrollResult(Long enrollResult, EnrollmentsDto request,
                                                     CoursesDto course, Long studentId) {
        switch (enrollResult.intValue()) {
            case 1:
                return CourseResponseWrapper.getFailed("您已经选过该课程");

            case 2:
                return CourseResponseWrapper.getFailed("该课程已经满人！");

            case 0:
                return enrollmentService.handleSuccessfulEnrollment(request, course, studentId);

            default:
                return CourseResponseWrapper.getFailed("未知错误");
        }
    }
}