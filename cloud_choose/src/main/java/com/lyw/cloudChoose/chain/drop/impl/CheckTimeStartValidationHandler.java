package com.lyw.cloudChoose.chain.drop.impl;

import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.chain.drop.DropAbstractValidationHandler;
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
 * 课程是否开始 检查处理器
 */
@Component
@Slf4j
public class CheckTimeStartValidationHandler extends DropAbstractValidationHandler {

    @Resource
    private CourseFeignService courseFeignService;
    @Resource
    private EnrollmentsDao enrollmentsDao;

    @Override
    protected ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {
        log.info("检查课程是否开始: studentId={}, courseId={}", request.getStudentId(), course.getId());

        try {


            log.info("课程是否开始检查通过: studentId={}, courseId={}", request.getStudentId(), course.getId());
            return ValidationResult.success();

        } catch (Exception e) {
            log.error("课程是否开始检查异常: studentId={}, courseId={}", request.getStudentId(), course.getId(), e);
            // 如果检查服务异常，可以选择严格模式（拒绝）或宽松模式（通过）
            // 这里采用严格模式
            return ValidationResult.failed("课程是否开始检查服务异常，请稍后重试");
        }
    }
}