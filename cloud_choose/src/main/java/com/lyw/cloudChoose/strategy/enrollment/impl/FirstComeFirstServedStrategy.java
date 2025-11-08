package com.lyw.cloudChoose.strategy.enrollment.impl;

import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.cloudChoose.vo.WaitlistsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.stereotype.Component;

import java.util.Date;
/**
 先到先得策略
 */
@Component("FIRST_COME")
public class FirstComeFirstServedStrategy implements EnrollmentStrategy {
    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        // 满课添加到等待队列中（课程容量检查处理器已经过滤掉不能加入等待队列的课程了）
        if (Boolean.TRUE.equals(course.getFullEnrolledCount())) {
            return addToWaitlist(request, course);
        }

        // 创建选课记录
        EnrollmentsVo enrollment = new EnrollmentsVo()
                .setStudentId(request.getStudentId())
                .setCourseId(request.getCourseId())
                .setEnrollmentType(request.getEnrollmentType())
                .setStatus("SUCCESS")
                .setEnrollmentSource(request.getEnrollmentSource())
                .setEnrolledAt(new Date());
        return CourseResponseWrapper.getSuccess(enrollment);

    }

    private CourseResponseWrapper addToWaitlist(EnrollmentsDto request, CoursesDto course) {
        WaitlistsVo waitlist = new WaitlistsVo()
                .setStudentId(request.getStudentId())
                .setCourseId(request.getCourseId())
                .setStatus("WAITING")
                .setJoinedAt(new Date())
                .setPriority(0);
        // 实现等待列表逻辑
        return CourseResponseWrapper.getSuccess("已加入等待列表", waitlist);
    }
}