package com.lyw.commonUtil.aspect;

import cn.hutool.core.util.StrUtil;
import com.lyw.commonUtil.annotation.MessageIdempotent;
import com.lyw.commonUtil.exception.DuplicateMessageException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class MessageIdempotentAspect {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String IDEMPOTENT_PREFIX = "msg:idempotent:";

    @Around("@annotation(messageIdempotent)")
    public Object around(ProceedingJoinPoint joinPoint, MessageIdempotent messageIdempotent) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 生成幂等键
        String idempotentKey = generateIdempotentKey(joinPoint, messageIdempotent);

        // 检查是否已处理
        if (isMessageProcessed(idempotentKey)) {
            log.warn("重复消息被拦截，幂等键: {}", idempotentKey);
            return handleDuplicateMessage(messageIdempotent, method);
        }

        // 标记为已处理
        markMessageProcessed(idempotentKey, messageIdempotent.expire());

        try {
            // 执行业务逻辑
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            // 业务执行失败，删除幂等标记
            deleteIdempotentKey(idempotentKey);
            throw e;
        }
    }

    /**
     * 生成幂等键
     */
    private String generateIdempotentKey(ProceedingJoinPoint joinPoint, MessageIdempotent messageIdempotent) {
        String keyExpression = messageIdempotent.key();

        if (StrUtil.isNotEmpty(keyExpression)) {
            // 使用SpEL表达式生成key
            return evaluateSpEL(joinPoint, keyExpression);
        } else {
            // 默认使用类名+方法名+参数hash
            return generateDefaultKey(joinPoint);
        }
    }

    /**
     * 解析SpEL表达式
     */
    private String evaluateSpEL(ProceedingJoinPoint joinPoint, String expression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        EvaluationContext context = new StandardEvaluationContext();

        // 设置参数
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        ExpressionParser parser = new SpelExpressionParser();
        return parser.parseExpression(expression).getValue(context, String.class);
    }

    /**
     * 生成默认key
     */
    private String generateDefaultKey(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();
        Object[] args = joinPoint.getArgs();

        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(className).append(".").append(methodName);

        if (args.length > 0) {
            String argsHash = DigestUtils.md5DigestAsHex(
                    Arrays.deepToString(args).getBytes(StandardCharsets.UTF_8)
            );
            keyBuilder.append(":").append(argsHash);
        }

        return keyBuilder.toString();
    }

    /**
     * 检查消息是否已处理
     */
    private boolean isMessageProcessed(String idempotentKey) {
        String redisKey = IDEMPOTENT_PREFIX + idempotentKey;
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    /**
     * 标记消息已处理
     */
    private void markMessageProcessed(String idempotentKey, long expire) {
        String redisKey = IDEMPOTENT_PREFIX + idempotentKey;
        redisTemplate.opsForValue().set(redisKey, "1", Duration.ofSeconds(expire));
    }

    /**
     * 删除幂等标记
     */
    private void deleteIdempotentKey(String idempotentKey) {
        String redisKey = IDEMPOTENT_PREFIX + idempotentKey;
        redisTemplate.delete(redisKey);
    }

    /**
     * 处理重复消息
     */
    private Object handleDuplicateMessage(MessageIdempotent messageIdempotent, Method method) {
        switch (messageIdempotent.handleType()) {
            case THROW_EXCEPTION:
                throw new DuplicateMessageException("消息重复消费");
            case RETURN_NULL:
            default:
                // 如果返回类型是void，返回null
                if (method.getReturnType().equals(void.class)) {
                    return null;
                }
                // 对于有返回值的方法，返回null
                return null;
        }
    }
}
