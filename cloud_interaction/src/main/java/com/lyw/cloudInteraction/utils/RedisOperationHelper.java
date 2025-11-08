package com.lyw.cloudInteraction.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.function.Supplier;

/**
 * Redis操作工具类
 */
@Slf4j
@Component
public class RedisOperationHelper {

    @Resource
    private RedisUtil redisUtil;

    // Redis操作重试次数
    private static final int DEFAULT_RETRY_COUNT = 2;
    // Redis操作重试间隔(毫秒)
    private static final long DEFAULT_RETRY_INTERVAL = 100L;

    /**
     * 检查Redis是否可用
     */
    public boolean isRedisAvailable() {
        try {
            redisUtil.hasKey("health_check");
            return true;
        } catch (Exception e) {
            log.warn("Redis连接不可用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 带重试的Redis操作
     */
    public <T> T executeWithRetry(Supplier<T> operation) {
        return executeWithRetry(operation, DEFAULT_RETRY_COUNT, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * 带重试的Redis操作（可自定义重试参数）
     */
    public <T> T executeWithRetry(Supplier<T> operation, int retryCount, long retryInterval) {
        Exception lastException = null;

        for (int i = 0; i < retryCount; i++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                log.warn("Redis操作第{}次失败: {}", i + 1, e.getMessage());

                if (i < retryCount - 1) {
                    try {
                        Thread.sleep(retryInterval);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        throw new RuntimeException("Redis操作失败", lastException);
    }

    /**
     * 执行无返回值的Redis操作
     */
    public void executeWithRetry(Runnable operation) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        });
    }

    /**
     * 更新Redis中的计数
     */
    public boolean updateCount(String countKey, Long targetId, long delta) {
        try {
            executeWithRetry(() -> {
                redisUtil.hIncrement(countKey, targetId.toString(), delta);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("更新Redis计数失败: {}/{}", countKey, targetId, e);
            return false;
        }
    }

    /**
     * 检查Hash字段是否存在
     */
    public boolean hashFieldExists(String key, String field) {
        try {
            return executeWithRetry(() -> redisUtil.hExists(key, field));
        } catch (Exception e) {
            log.warn("检查Hash字段失败: {}/{}", key, field, e);
            return false;
        }
    }

    /**
     * 设置Hash字段
     */
    public boolean hashSet(String key, String field, Object value) {
        try {
            executeWithRetry(() -> {
                redisUtil.hSet(key, field, value);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("设置Hash字段失败: {}/{}", key, field, e);
            return false;
        }
    }

    /**
     * 删除Hash字段
     */
    public boolean hashDelete(String key, String field) {
        try {
            executeWithRetry(() -> {
                redisUtil.hDelete(key, field);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("删除Hash字段失败: {}/{}", key, field, e);
            return false;
        }
    }
}