package com.lyw.cloudChoose.strategy.enrollment.impl;

import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 优先级策略
 */
@Component("PRIORITY_BASED")
public class PriorityBasedStrategy implements EnrollmentStrategy {
    @Override
    public CourseResponseWrapper enroll(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        int priority = calculateStudentPriority(request, strategy);

        if (Boolean.TRUE.equals(course.getFullEnrolledCount())) {
            // 根据优先级决定是否替换低优先级学生
            return handlePriorityEnrollment(request, course, strategy, priority);
        } else {
            // 直接选课成功
            EnrollmentsVo enrollment = new EnrollmentsVo()
                    .setStudentId(request.getStudentId())
                    .setCourseId(request.getCourseId())
                    .setEnrollmentType(request.getEnrollmentType())
                    .setStatus("SUCCESS")
                    .setPriority(priority)
                    .setEnrolledAt(new Date());
            // 调用课程服务增加选课人数
            return CourseResponseWrapper.getSuccess("选课成功", enrollment);
        }
    }

    private int calculateStudentPriority(EnrollmentsDto request, EnrollmentStrategiesVo strategy) {
        // 实现复杂的优先级计算逻辑
        // 可以考虑专业相关度、学业排名、学分需求等因素
        return 85; // 示例优先级分数
    }

    private CourseResponseWrapper handlePriorityEnrollment(EnrollmentsDto request, CoursesDto course,
                                                           EnrollmentStrategiesVo strategy, int priority) {
        // 实现优先级替换逻辑
        return CourseResponseWrapper.getFailed("课程容量已满，但您的优先级不足以替换现有学生");
    }
}
