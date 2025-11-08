package com.lyw.cloudChoose.chain.enrollment.impl;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentAbstractValidationHandler;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.mapper.EnrollmentBlacklistDao;
import com.lyw.cloudChoose.vo.EnrollmentBlacklistVo;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 黑名单检查处理器
 */
@Component
@Slf4j
public class BlacklistValidationHandler extends EnrollmentAbstractValidationHandler {

    @Resource
    private EnrollmentBlacklistDao enrollmentBlacklistDao;

    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {
        log.info("检查学生是否在黑名单中: studentId={}", request.getStudentId());

        // 检查学生是否在黑名单中
        EnrollmentBlacklistVo blacklist = enrollmentBlacklistDao.selectByStudentId(request.getStudentId());
        if (ObjectUtil.isNotEmpty(blacklist)) {
            String reason = String.format("学生处于黑名单中，禁止选课。原因: %s", blacklist.getReason());
            log.warn("黑名单检查失败: studentId={}, reason={}", request.getStudentId(), blacklist.getReason());
            return ValidationResult.failed(reason);
        }

        // 检查课程是否在黑名单中（针对特定学生）
        EnrollmentBlacklistVo courseBlacklist = enrollmentBlacklistDao.selectByStudentAndCourse(
                request.getStudentId(), request.getCourseId());
        if (ObjectUtil.isNotEmpty(courseBlacklist)) {
            String reason = String.format("该课程对您受限，禁止选课。原因: %s", courseBlacklist.getReason());
            log.warn("课程黑名单检查失败: studentId={}, courseId={}, reason={}",
                    request.getStudentId(), request.getCourseId(), courseBlacklist.getReason());
            return ValidationResult.failed(reason);
        }

        log.info("黑名单检查通过: studentId={}", request.getStudentId());
        return ValidationResult.success();
    }
}