package com.lyw.cloudChoose.factory;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentValidationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 选课验证责任链工厂
 */
@Component
public class EnrollmentValidationChainFactory {

    @Autowired
    private List<EnrollmentValidationHandler> validationHandlers;

    /**
     * 创建验证责任链
     */
    public EnrollmentValidationHandler createValidationChain() {
        if (CollectionUtil.isEmpty(validationHandlers)) {
            return null;
        }

        // 构建责任链
        EnrollmentValidationHandler firstHandler = validationHandlers.get(0);
        EnrollmentValidationHandler currentHandler = firstHandler;

        for (int i = 1; i < validationHandlers.size(); i++) {
            EnrollmentValidationHandler nextHandler = validationHandlers.get(i);
            currentHandler.setNextHandler(nextHandler);
            currentHandler = nextHandler;
        }

        return firstHandler;
    }
}