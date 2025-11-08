package com.lyw.cloudChoose.chain.enrollment;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.chain.ValidationContext;
import com.lyw.cloudChoose.chain.ValidationResult;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentStrategiesVo;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象验证处理器
 */
@Slf4j
public abstract class EnrollmentAbstractValidationHandler implements EnrollmentValidationHandler {

    protected EnrollmentValidationHandler nextHandler;

    @Override
    public void setNextHandler(EnrollmentValidationHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    /**
     * 传递给下一个处理器
     */
    protected ValidationResult passToNext(EnrollmentsDto request, CoursesDto course,
                                          EnrollmentStrategiesVo strategy, ValidationContext context) {
        if (ObjectUtil.isNotEmpty(nextHandler)) {
            return nextHandler.handle(request, course, strategy, context);
        }
        return ValidationResult.success();
    }

    /**
     * 验证逻辑
     */
    protected abstract ValidationResult doValidate(EnrollmentsDto request, CoursesDto course,
                                                   EnrollmentStrategiesVo strategy, ValidationContext context);

    @Override
    public ValidationResult handle(EnrollmentsDto request, CoursesDto course,
                                   EnrollmentStrategiesVo strategy, ValidationContext context) {
        log.debug("执行验证处理器: {}", getClass().getSimpleName());

        ValidationResult result = doValidate(request, course, strategy, context);
        result.setHandlerName(getClass().getSimpleName());

        if (!result.isValid()) {
            log.warn("验证失败: handler={}, reasons={}", getClass().getSimpleName(), result.getRejectionReasons());
            return result;
        }

        return passToNext(request, course, strategy, context);
    }
}