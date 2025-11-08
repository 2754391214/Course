package com.lyw.cloudChoose.factory;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudChoose.chain.drop.DropValidationHandler;
import com.lyw.cloudChoose.chain.enrollment.EnrollmentValidationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 退课验证责任链工厂
 */
@Component
public class DropValidationChainFactory {

    @Autowired
    private List<DropValidationHandler> validationHandlers;

    /**
     * 创建验证责任链
     */
    public DropValidationHandler createValidationChain() {
        if (CollectionUtil.isEmpty(validationHandlers)) {
            return null;
        }

        // 构建责任链
        DropValidationHandler firstHandler = validationHandlers.get(0);
        DropValidationHandler currentHandler = firstHandler;

        for (int i = 1; i < validationHandlers.size(); i++) {
            DropValidationHandler nextHandler = validationHandlers.get(i);
            currentHandler.setNextHandler(nextHandler);
            currentHandler = nextHandler;
        }

        return firstHandler;
    }
}