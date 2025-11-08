package com.lyw.cloudChoose.chain.enrollment.impl;

import com.lyw.cloudChoose.chain.enrollment.EnrollmentAbstractValidationHandler;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 课程容量检查处理器
 */
@Component
@Slf4j
public class CapacityValidationHandler extends EnrollmentAbstractValidationHandler {

    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {

        if ("WAITLIST".equals(request.getEnrollmentType())){
            log.info("等待队列不需要检查课程容量");
            return ValidationResult.success();
        }

        log.info("检查课程容量: courseId={}, capacity={}, enrolled={}",
                course.getId(), course.getCapacity(), course.getEnrolledCount());
        // 检查课程容量
        if (course.getEnrolledCount() >= course.getCapacity()) {
            if (Boolean.TRUE.equals(strategy.getAutoWaitlist())) {
                log.info("课程容量已满，根据课程规则是否允许加入等待列表或替换低优先级学生: courseId={}", course.getId());
                course.setFullEnrolledCount(true);
            } else {
                log.warn("课程容量检查失败: courseId={}, capacity={}, enrolled={}",
                        course.getId(), course.getCapacity(), course.getEnrolledCount());
                return ValidationResult.failed("课程容量已满");
            }
        }

        log.info("课程容量检查通过: courseId={}", course.getId());
        return ValidationResult.success();
    }
}