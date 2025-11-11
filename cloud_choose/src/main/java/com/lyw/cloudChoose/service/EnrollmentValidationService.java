package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;

public interface EnrollmentValidationService {
    ValidationResult validateEnrollment(EnrollmentsDto request, CoursesDto course,
                                        EnrollmentStrategiesVo strategy);

    ValidationResult validateDrop(EnrollmentsDto dto, CoursesDto course,
                                  EnrollmentStrategiesVo strategy);
}
