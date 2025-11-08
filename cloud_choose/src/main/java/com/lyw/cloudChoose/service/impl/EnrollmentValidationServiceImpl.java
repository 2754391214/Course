package com.lyw.cloudChoose.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.chain.drop.DropValidationHandler;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentValidationHandler;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.factory.DropValidationChainFactory;
import com.lyw.cloudChoose.factory.EnrollmentValidationChainFactory;
import com.lyw.cloudChoose.service.EnrollmentValidationService;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnrollmentValidationServiceImpl implements EnrollmentValidationService {
    @Autowired
    private EnrollmentValidationChainFactory enrollmentValidationChainFactory;
    @Autowired
    private DropValidationChainFactory dropValidationChainFactory;

    /**
     * 执行选课验证
     */
    @Override
    public ValidationResult validateEnrollment(EnrollmentsDto request, CoursesDto course,
                                               EnrollmentStrategiesVo strategy) {
        log.info("开始执行选课验证: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());

        ValidationContext context = new ValidationContext();
        EnrollmentValidationHandler validationChain = enrollmentValidationChainFactory.createValidationChain();

        if (ObjectUtil.isEmpty(validationChain)) {
            log.warn("验证责任链为空，跳过验证");
            return ValidationResult.success();
        }

        ValidationResult result = validationChain.handle(request, course, strategy, context);

        log.info("选课验证完成: studentId={}, courseId={}, valid={}",
                request.getStudentId(), request.getCourseId(), result.isValid());

        return result;
    }
    /**
     * 执行退选验证
     */
    @Override
    public ValidationResult validateDrop(EnrollmentsDto request, CoursesDto course, EnrollmentStrategiesVo strategy) {
        log.info("开始执行退课验证: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());

        ValidationContext context = new ValidationContext();
        DropValidationHandler validationChain = dropValidationChainFactory.createValidationChain();

        if (ObjectUtil.isEmpty(validationChain)) {
            log.warn("验证责任链为空，跳过验证");
            return ValidationResult.success();
        }

        ValidationResult result = validationChain.handle(request, course, strategy, context);

        log.info("退课验证完成: studentId={}, courseId={}, valid={}",
                request.getStudentId(), request.getCourseId(), result.isValid());

        return result;
    }
}
