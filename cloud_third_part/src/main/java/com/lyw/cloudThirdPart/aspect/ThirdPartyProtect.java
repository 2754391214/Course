package com.lyw.cloudThirdPart.aspect;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface ThirdPartyProtect {

    /**
     * 服务类型
     */
    ServiceType service();

    /**
     * 时间窗口内的最大调用次数
     */
    int count() default 10;

    /**
     * 时间窗口（毫秒）
     */
    long timeWindow() default 60000;

    /**
     * 触发限制后的冷却时间（秒）
     */
    long coolDown() default 300;

    /**
     * 成本敏感度：LOW-宽松, MEDIUM-中等, HIGH-严格
     */
    CostSensitivity sensitivity() default CostSensitivity.MEDIUM;

    /**
     * 是否启用业务上下文验证
     */
    boolean enableContextCheck() default true;

    /**
     * 自定义业务规则（SpEL表达式）
     */
    String businessRule() default "";

    enum ServiceType {
        SMS,        // 短信服务
        EMAIL,      // 邮件服务
        OSS,        // 对象存储
        PAYMENT,    // 支付服务
        AI_SERVICE, // AI服务
        CUSTOM      // 自定义服务
    }

    enum CostSensitivity {
        LOW,    // 低成本服务，限制较宽松
        MEDIUM, // 中等成本，平衡限制
        HIGH    // 高成本服务，严格限制
    }
}