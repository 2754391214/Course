package com.lyw.cloudChoose.chain.enrollment.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentAbstractValidationHandler;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 基础信息验证处理器
 */
@Component("EnrollmentBasicInfoValidationHandler")
@Slf4j
public class BasicInfoValidationHandler extends EnrollmentAbstractValidationHandler {
    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {
        log.info("执行基础信息验证: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());

        ValidationResult result = ValidationResult.success();

        // 验证课程是否存在
        if (ObjectUtil.isEmpty(course)) {
            result.addRejectionReason("课程不存在");
            return result;
        }

        // 验证课程状态
        if (!"PUBLISHED".equals(course.getStatus())) {
            result.addRejectionReason("课程未发布或已关闭");
        }

        // 验证选课类型
        if (!Arrays.asList("NORMAL", "AUDIT").contains(request.getEnrollmentType())) {
            result.addRejectionReason("无效的选课类型");
        }

        // 验证旁听权限
        if ("AUDIT".equals(request.getEnrollmentType()) &&
                !Boolean.TRUE.equals(strategy.getAllowAudit())) {
            result.addRejectionReason("该课程不允许旁听");
        }

        if (result.isValid()) {
            log.info("基础信息验证通过: studentId={}, courseId={}", request.getStudentId(), request.getCourseId());
        }

        return result;
    }
}