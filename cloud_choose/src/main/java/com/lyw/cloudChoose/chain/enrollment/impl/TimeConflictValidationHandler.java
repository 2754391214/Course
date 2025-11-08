package com.lyw.cloudChoose.chain.enrollment.impl;

import com.lyw.cloudChoose.chain.enrollment.EnrollmentAbstractValidationHandler;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.feign.CourseFeignService;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 时间冲突检查处理器
 */
@Component
@Slf4j
public class TimeConflictValidationHandler extends EnrollmentAbstractValidationHandler {

    @Resource
    private CourseFeignService courseFeignService;
    @Resource
    private EnrollmentsDao enrollmentsDao;

    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {

        // 如果策略不要求检查时间冲突，则跳过
        if (!Boolean.TRUE.equals(strategy.getConflictCheck())) {
            log.info("跳过时间冲突检查: courseId={}", course.getId());
            return ValidationResult.success();
        }

        log.info("检查时间冲突: studentId={}, courseId={}", request.getStudentId(), course.getId());

        try {
            List<Long> enrolledCourseIds = enrollmentsDao.selectEnrolledCourseIds(request.getStudentId());
            // 调用课程服务检查时间冲突
            CourseResponseWrapper response = courseFeignService.checkTimeConflict(
                    course.getId(),request.getStudentId(),enrolledCourseIds);

            if (!response.isSuccess()) {
                log.warn("时间冲突检查失败: studentId={}, courseId={}", request.getStudentId(), course.getId());
                return ValidationResult.failed(response.getErrorMessage());
            }

            log.info("时间冲突检查通过: studentId={}, courseId={}", request.getStudentId(), course.getId());
            return ValidationResult.success();

        } catch (Exception e) {
            log.error("时间冲突检查异常: studentId={}, courseId={}", request.getStudentId(), course.getId(), e);
            // 如果检查服务异常，可以选择严格模式（拒绝）或宽松模式（通过）
            // 这里采用严格模式
            return ValidationResult.failed("时间冲突检查服务异常，请稍后重试");
        }
    }
}