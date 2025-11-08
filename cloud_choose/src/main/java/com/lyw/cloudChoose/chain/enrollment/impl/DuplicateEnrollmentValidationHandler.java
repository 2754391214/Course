package com.lyw.cloudChoose.chain.enrollment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentAbstractValidationHandler;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.mapper.EnrollmentsDao;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import com.lyw.cloudChoose.vo.EnrollmentsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 重复选课检查处理器
 */
@Component
@Slf4j
public class DuplicateEnrollmentValidationHandler extends EnrollmentAbstractValidationHandler {

    @Autowired
    private EnrollmentsDao enrollmentsDao;

    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {
        log.info("检查是否重复选课: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());

        // 查询学生是否已经选过该课程
        List<EnrollmentsVo> existingEnrollments = enrollmentsDao.selectList(
                new LambdaQueryWrapper<EnrollmentsVo>()
                        .eq(EnrollmentsVo::getStudentId, request.getStudentId())
                        .eq(EnrollmentsVo::getCourseId, request.getCourseId())
        );

        // 检查是否存在有效的选课记录
        boolean hasActiveEnrollment = existingEnrollments.stream()
                .anyMatch(enrollment ->
                        Arrays.asList("SUCCESS", "PENDING", "WAITING").contains(enrollment.getStatus()));

        if (hasActiveEnrollment) {
            log.warn("重复选课检查失败: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());
            return ValidationResult.failed("您已选过该课程");
        }

        log.info("重复选课检查通过: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());
        return ValidationResult.success();
    }
}