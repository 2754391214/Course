package com.lyw.commonUtil.util.reidsCache.StringCache;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.commonUtil.util.RedissLockUtil;
import com.lyw.commonUtil.util.reidsCache.CacheLoader;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 分布式缓存工具类
 */
@Component
@Slf4j
public class DistributedCacheHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissLockUtil redissLockUtil;

    /**
     * 获取或加载缓存数据（通用方法）
     */
    public <T> T getOrLoad(String keySuffix, CacheConfig config, CacheLoader<T> loader, Class<T> type) {
        long startTime = System.currentTimeMillis();
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        String lockKey = config.getLockKeyPrefix() + keySuffix;

        try {
            // 1. 先尝试从缓存获取
            CacheResult<T> cacheResult = getFromCache(cacheKey, type);
            T value = cacheResult.getData();
            if (cacheResult.hasValidData()) {
                log.info("缓存命中 [key:{}], 耗时: {}ms", cacheKey, System.currentTimeMillis() - startTime);
                return value;
            }

            // 2. 缓存未命中，尝试获取分布式锁
            boolean locked = redissLockUtil.tryLock(lockKey, config.getLockWaitTime(), config.getLockLeaseTime());
            if (!locked) {
                log.warn("获取分布式锁失败 [key:{}], 降级到直接加载", lockKey);
                return loadWithFallback(loader, "获取锁失败降级");
            }

            try {
                log.debug("成功获取分布式锁 [key:{}]", lockKey);

                // 3. 双重检查
                cacheResult = getFromCache(cacheKey, type);
                value = cacheResult.getData();
                if (ObjectUtil.isNotEmpty(value)) {
                    log.info("双重检查缓存命中 [key:{}], 耗时: {}ms",
                            cacheKey, System.currentTimeMillis() - startTime);
                    return value;
                }

                // 4. 加载数据
                long loadStartTime = System.currentTimeMillis();
                value = loader.load();
                log.debug("数据加载耗时: {}ms [key:{}]",
                        System.currentTimeMillis() - loadStartTime, cacheKey);

                // 5. 设置缓存
                setToCache(cacheKey, value, config);
                log.info("数据加载并设置缓存完成 [key:{}], 总耗时: {}ms",
                        cacheKey, System.currentTimeMillis() - startTime);

                return value;

            } finally {
                // 6. 释放锁
                redissLockUtil.unlock(lockKey);
                log.debug("释放分布式锁 [key:{}]", lockKey);
            }

        } catch (Exception e) {
            log.warn("缓存获取失败 [key:{}], 降级到直接加载", cacheKey, e);
            return loadWithFallback(loader, "异常降级");
        }
    }

    /**
     * 从缓存获取数据
     */
    @SuppressWarnings("unchecked")
    private <T> CacheResult<T> getFromCache(String cacheKey, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(cacheKey);
        if (value instanceof String && ((String) value).isEmpty()) {
            // 空值标记，返回null
            return new CacheResult<>(true, null);
        }
        return new CacheResult<>(false,(T) value);
    }

    /**
     * 设置缓存
     */
    private <T> void setToCache(String cacheKey, T value, CacheConfig config) {
        if (ObjectUtil.isEmpty(value)) {
            if (config.isCacheNullValue()) {
                // 防止缓存穿透，设置空值标记
                redisTemplate.opsForValue().set(cacheKey, "", config.getNullValueTimeout());
                log.debug("设置空值缓存防止穿透 [key:{}]", cacheKey);
            }
        } else {
            redisTemplate.opsForValue().set(cacheKey, value, config.getCacheTimeout());
        }
    }

    /**
     * 降级加载数据
     */
    private <T> T loadWithFallback(CacheLoader<T> loader, String reason) {
        try {
            return loader.load();
        } catch (Exception e) {
            log.warn("降级加载失败: {}", reason, e);
            try {
                return loader.handleNull();
            } catch (Exception ex) {
                log.error("处理空值失败", ex);
                return null;
            }
        }
    }

    /**
     * 删除缓存
     */
    public void evict(String keySuffix, CacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        redisTemplate.delete(cacheKey);
        log.debug("删除缓存 [key:{}]", cacheKey);
    }
    /**
     * 缓存结果包装类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class CacheResult<T> {
        private boolean isNullValue;   // 是否是空值标记
        private T data;               // 实际数据

        public boolean hasValidData() {
            return isNullValue || data != null;
        }
    }
}
