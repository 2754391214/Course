package com.lyw.commonUtil.util.reidsCache.HashCache;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.commonUtil.util.RedissLockUtil;
import com.lyw.commonUtil.util.reidsCache.CacheLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 分布式Hash缓存工具类
 */
@Component
@Slf4j
public class DistributedHashCacheHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissLockUtil redissLockUtil;

    /**
     * 获取或加载Hash缓存数据（通用方法）
     * @param keySuffix 缓存键后缀
     * @param hashField Hash字段
     * @param config 缓存配置
     * @param loader 数据加载器
     * @param type 返回值类型
     * @return 缓存数据
     */
    public <T> T getOrLoadHash(String keySuffix, String hashField,
                               HashCacheConfig config, CacheLoader<T> loader, Class<T> type) {
        long startTime = System.currentTimeMillis();
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        String lockKey = config.getLockKeyPrefix() + keySuffix + ":" + hashField;

        try {
            // 1. 先尝试从缓存获取
            T value = getFromHashCache(cacheKey, hashField, type);
            if (ObjectUtil.isNotEmpty(value)) {
                log.info("Hash缓存命中 [key:{}, field:{}], 耗时: {}ms",
                        cacheKey, hashField, System.currentTimeMillis() - startTime);
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
                value = getFromHashCache(cacheKey, hashField, type);
                if (ObjectUtil.isNotEmpty(value)) {
                    log.info("双重检查Hash缓存命中 [key:{}, field:{}], 耗时: {}ms",
                            cacheKey, hashField, System.currentTimeMillis() - startTime);
                    return value;
                }

                // 4. 加载数据
                long loadStartTime = System.currentTimeMillis();
                value = loader.load();
                log.debug("Hash数据加载耗时: {}ms [key:{}, field:{}]",
                        System.currentTimeMillis() - loadStartTime, cacheKey, hashField);

                // 5. 设置缓存
                setToHashCache(cacheKey, hashField, value, config);
                log.info("Hash数据加载并设置缓存完成 [key:{}, field:{}], 总耗时: {}ms",
                        cacheKey, hashField, System.currentTimeMillis() - startTime);

                return value;

            } finally {
                // 6. 释放锁
                redissLockUtil.unlock(lockKey);
                log.debug("释放分布式锁 [key:{}]", lockKey);
            }

        } catch (Exception e) {
            log.warn("Hash缓存获取失败 [key:{}, field:{}], 降级到直接加载", cacheKey, hashField, e);
            return loadWithFallback(loader, "异常降级");
        }
    }

    /**
     * 批量获取Hash缓存数据
     */
    public <T> Map<String, T> batchGetHash(String keySuffix, List<String> hashFields,
                                           Class<T> type, HashCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;

        HashOperations<String, String, Object> hashOps = redisTemplate.opsForHash();
        List<Object> values = hashOps.multiGet(cacheKey, hashFields);

        return IntStream.range(0, hashFields.size())
                .boxed()
                .collect(Collectors.toMap(
                        hashFields::get,
                        i -> {
                            Object value = values.get(i);
                            if (value instanceof String && ((String) value).isEmpty()) {
                                return null;
                            }
                            return value != null ? type.cast(value) : null;
                        }
                ));
    }

    /**
     * 批量设置Hash缓存数据
     */
    public <T> void batchSetHash(String keySuffix, Map<String, T> fieldValueMap, HashCacheConfig config) {
        if (ObjectUtil.isEmpty(fieldValueMap)) {
            return;
        }

        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        redisTemplate.opsForHash().putAll(cacheKey, fieldValueMap);

        // 设置过期时间
        redisTemplate.expire(cacheKey, config.getCacheTimeout(), config.getTimeUnit());

        log.debug("批量设置Hash缓存完成 [key:{}, fields:{}]", cacheKey, fieldValueMap.keySet());
    }

    /**
     * 从Hash缓存获取数据
     */
    @SuppressWarnings("unchecked")
    private <T> T getFromHashCache(String cacheKey, String hashField, Class<T> type) {
        Object value = redisTemplate.opsForHash().get(cacheKey, hashField);
        if (value instanceof String && ((String) value).isEmpty()) {
            // 空值标记，返回null
            return null;
        }
        return (T) value;
    }

    /**
     * 设置Hash缓存
     */
    private <T> void setToHashCache(String cacheKey, String hashField, T value, HashCacheConfig config) {
        if (ObjectUtil.isEmpty(value)) {
            if (config.isCacheNullValue()) {
                // 防止缓存穿透，设置空值标记
                redisTemplate.opsForHash().put(cacheKey, hashField, "");
                // 设置整个Hash的过期时间
                redisTemplate.expire(cacheKey, config.getNullValueTimeout(), config.getTimeUnit());
                log.debug("设置Hash空值缓存防止穿透 [key:{}, field:{}]", cacheKey, hashField);
            }
        } else {
            redisTemplate.opsForHash().put(cacheKey, hashField, value);
            // 设置整个Hash的过期时间
            redisTemplate.expire(cacheKey, config.getCacheTimeout(), config.getTimeUnit());
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
     * 删除Hash缓存中的指定字段
     */
    public void evictHashField(String keySuffix, String hashField, HashCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        redisTemplate.opsForHash().delete(cacheKey, hashField);
        log.debug("删除Hash缓存字段 [key:{}, field:{}]", cacheKey, hashField);
    }

    /**
     * 删除整个Hash缓存
     */
    public void evictHash(String keySuffix, HashCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        redisTemplate.delete(cacheKey);
        log.debug("删除Hash缓存 [key:{}]", cacheKey);
    }

    /**
     * 检查Hash字段是否存在
     */
    public boolean hasHashField(String keySuffix, String hashField, HashCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        return redisTemplate.opsForHash().hasKey(cacheKey, hashField);
    }
}