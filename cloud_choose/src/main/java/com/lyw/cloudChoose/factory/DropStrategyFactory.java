package com.lyw.cloudChoose.factory;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudChoose.strategy.drop.DropStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class DropStrategyFactory {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, DropStrategy> strategyMap = new HashMap<>();

    @PostConstruct
    public void init() {
        // 注册所有退选策略
        Map<String, DropStrategy> strategies = applicationContext.getBeansOfType(DropStrategy.class);
        for (DropStrategy strategy : strategies.values()) {
            String strategyType = getStrategyType(strategy.getClass());
            strategyMap.put(strategyType, strategy);
        }
    }

    public DropStrategy getStrategy(String strategyType) {
        DropStrategy strategy = strategyMap.get(strategyType);
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
