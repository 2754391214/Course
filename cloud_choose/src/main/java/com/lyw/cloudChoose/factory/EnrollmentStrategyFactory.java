package com.lyw.cloudChoose.factory;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.strategy.enrollment.EnrollmentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class EnrollmentStrategyFactory {
    @Autowired
    private ApplicationContext applicationContext;
    private final Map<String, EnrollmentStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册所有退选策略
        Map<String, EnrollmentStrategy> strategies = applicationContext.getBeansOfType(EnrollmentStrategy.class);
        for ( EnrollmentStrategy strategy : strategies.values()) {
            String strategyType = getStrategyType(strategy.getClass());
            strategyMap.put(strategyType, strategy);
        }
    }

    public EnrollmentStrategy getStrategy(String strategyType) {
        EnrollmentStrategy strategy = strategyMap.get(strategyType);
        if (ObjectUtil.isEmpty(strategy)) {
            throw new IllegalArgumentException("不支持的退选策略类型: " + strategyType);
        }
        return strategy;
    }

    private String getStrategyType(Class<?> strategyClass) {
        Component component = strategyClass.getAnnotation(Component.class);
        return component.value();
    }
}