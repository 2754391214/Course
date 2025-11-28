package com.lyw.commonUtil.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MessageIdempotent {

    /**
     * 幂等键的SpEL表达式，用于生成唯一标识
     */
    String key() default "";

    /**
     * 过期时间（秒），默认5分钟
     */
    long expire() default 300;

    /**
     * 重复消息处理策略
     */
    HandleType handleType() default HandleType.RETURN_NULL;

    enum HandleType {
        RETURN_NULL,    // 直接返回空结果
        THROW_EXCEPTION // 抛出异常
    }
}
