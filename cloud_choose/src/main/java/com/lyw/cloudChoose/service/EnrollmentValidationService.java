package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;

/**
 * 选课验证服务
 */
public interface EnrollmentValidationService {

    /**
     * 执行选课验证
     */
    ValidationResult validateEnrollment(EnrollmentsDto request, CoursesDto course,
                                               EnrollmentStrategiesVo strategy);

    /**
     * 执行退选验证
     */
    ValidationResult validateDrop(EnrollmentsDto request, CoursesDto course,
                                         EnrollmentStrategiesVo strategy);
}