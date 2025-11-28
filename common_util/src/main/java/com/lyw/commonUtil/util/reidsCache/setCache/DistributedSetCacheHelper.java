package com.lyw.commonUtil.util.reidsCache.setCache;

import cn.hutool.core.util.ObjectUtil;
import com.lyw.commonUtil.util.RedissLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分布式Set缓存工具类
 */
@Component
@Slf4j
public class DistributedSetCacheHelper {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedissLockUtil redissLockUtil;

    // 空值标记
    private static final String EMPTY_MARKER = "EMPTY_MARKER";

    /**
     * 获取或加载Set缓存数据（通用方法）
     */
    public <T> Set<T> getOrLoad(String keySuffix, SetCacheConfig config, SetCacheLoader<T> loader, Class<T> type) {
        long startTime = System.currentTimeMillis();
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        String lockKey = config.getLockKeyPrefix() + keySuffix;

        try {
            // 1. 先尝试从缓存获取
            Set<T> value = getFromCache(cacheKey, type);
            if (value != null) { // 修改：使用 null 判断而不是 isEmpty
                log.info("Set缓存命中 [key:{}], 元素数量: {}, 耗时: {}ms",
                        cacheKey, value.size(), System.currentTimeMillis() - startTime);
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
                value = getFromCache(cacheKey, type);
                if (value != null) { // 修改：使用 null 判断而不是 isEmpty
                    log.info("双重检查Set缓存命中 [key:{}], 元素数量: {}, 耗时: {}ms",
                            cacheKey, value.size(), System.currentTimeMillis() - startTime);
                    return value;
                }

                // 4. 加载数据
                long loadStartTime = System.currentTimeMillis();
                value = loader.load();
                log.debug("Set数据加载耗时: {}ms [key:{}], 元素数量: {}",
                        System.currentTimeMillis() - loadStartTime, cacheKey,
                        value != null ? value.size() : 0);

                // 5. 设置缓存
                setToCache(cacheKey, value, config);
                log.info("Set数据加载并设置缓存完成 [key:{}], 元素数量: {}, 总耗时: {}ms",
                        cacheKey, value != null ? value.size() : 0,
                        System.currentTimeMillis() - startTime);

                return value;

            } finally {
                // 6. 释放锁
                redissLockUtil.unlock(lockKey);
                log.debug("释放分布式锁 [key:{}]", lockKey);
            }

        } catch (Exception e) {
            log.warn("Set缓存获取失败 [key:{}], 降级到直接加载", cacheKey, e);
            return loadWithFallback(loader, "异常降级");
        }
    }

    /**
     * 从Set缓存获取数据（修复版本）
     */
    @SuppressWarnings("unchecked")
    private <T> Set<T> getFromCache(String cacheKey, Class<T> type) {
        try {
            Set<Object> members = redisTemplate.opsForSet().members(cacheKey);

            if (members == null || members.isEmpty()) {
                return null; // 返回null表示缓存不存在
            }

            // 检查是否为空值标记
            if (members.size() == 1) {
                Object firstMember = members.iterator().next();
                if (EMPTY_MARKER.equals(firstMember)) {
                    return Collections.emptySet(); // 返回空集合表示确实没有数据
                }
            }

            // 安全类型转换
            Set<T> result = new HashSet<>();
            for (Object member : members) {
                if (EMPTY_MARKER.equals(member)) {
                    continue; // 跳过空值标记
                }

                try {
                    T convertedValue = convertToType(member, type);
                    if (convertedValue != null) {
                        result.add(convertedValue);
                    }
                } catch (Exception e) {
                    log.warn("类型转换失败, 值: {}, 目标类型: {}", member, type.getSimpleName());
                }
            }

            return result;

        } catch (Exception e) {
            log.error("从缓存获取数据失败: key={}", cacheKey, e);
            return null; // 发生异常时返回null，触发重新加载
        }
    }

    /**
     * 类型安全转换
     */
    @SuppressWarnings("unchecked")
    private <T> T convertToType(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        // 如果已经是目标类型，直接返回
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // 处理数字类型转换
        if (targetType == Long.class) {
            if (value instanceof Integer) {
                return (T) Long.valueOf(((Integer) value).longValue());
            } else if (value instanceof String) {
                return (T) Long.valueOf((String) value);
            } else if (value instanceof Number) {
                return (T) Long.valueOf(((Number) value).longValue());
            }
        }

        if (targetType == Integer.class) {
            if (value instanceof Long) {
                return (T) Integer.valueOf(((Long) value).intValue());
            } else if (value instanceof String) {
                return (T) Integer.valueOf((String) value);
            } else if (value instanceof Number) {
                return (T) Integer.valueOf(((Number) value).intValue());
            }
        }

        if (targetType == String.class) {
            return (T) value.toString();
        }

        log.warn("无法转换类型: {} -> {}", value.getClass().getSimpleName(), targetType.getSimpleName());
        return null;
    }

    /**
     * 设置Set缓存（修复版本）
     */
    private <T> void setToCache(String cacheKey, Set<T> value, SetCacheConfig config) {
        try {
            // 先删除现有key
            redisTemplate.delete(cacheKey);

            if (ObjectUtil.isEmpty(value)) {
                if (config.isCacheNullValue()) {
                    // 防止缓存穿透，设置空值标记（使用字符串）
                    redisTemplate.opsForSet().add(cacheKey, EMPTY_MARKER);
                    redisTemplate.expire(cacheKey, config.getNullValueTimeout(), config.getTimeUnit());
                    log.debug("设置Set空值缓存防止穿透 [key:{}]", cacheKey);
                }
                // 如果是空集合且不缓存空值，就不设置缓存
            } else {
                // 确保所有元素都是字符串形式存储，避免序列化问题
                String[] stringMembers = value.stream()
                        .map(Object::toString)
                        .toArray(String[]::new);

                redisTemplate.opsForSet().add(cacheKey, stringMembers);
                redisTemplate.expire(cacheKey, config.getCacheTimeout(), config.getTimeUnit());

                log.debug("设置Set缓存完成 [key:{}, 元素数量:{}]", cacheKey, value.size());
            }
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", cacheKey, e);
        }
    }

    /**
     * 降级加载数据
     */
    private <T> Set<T> loadWithFallback(SetCacheLoader<T> loader, String reason) {
        try {
            return loader.load();
        } catch (Exception e) {
            log.warn("降级加载失败: {}", reason, e);
            try {
                return loader.handleNull();
            } catch (Exception ex) {
                log.error("处理空值失败", ex);
                return Collections.emptySet();
            }
        }
    }

    /**
     * 向Set缓存添加元素
     */
    public <T> Long addToSet(String keySuffix, T element, SetCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;

        // 先检查是否存在空值标记
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(cacheKey, EMPTY_MARKER))) {
            redisTemplate.delete(cacheKey); // 删除空值标记
        }

        return redisTemplate.opsForSet().add(cacheKey, element.toString());
    }

    /**
     * 批量向Set缓存添加元素
     */
    public <T> Long addToSet(String keySuffix, Set<T> elements, SetCacheConfig config) {
        if (ObjectUtil.isEmpty(elements)) {
            return 0L;
        }

        String cacheKey = config.getCacheKeyPrefix() + keySuffix;

        // 先检查是否存在空值标记
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(cacheKey, EMPTY_MARKER))) {
            redisTemplate.delete(cacheKey); // 删除空值标记
        }

        String[] members = elements.stream()
                .map(Object::toString)
                .toArray(String[]::new);

        return redisTemplate.opsForSet().add(cacheKey, members);
    }

    /**
     * 从Set缓存移除元素
     */
    public <T> Long removeFromSet(String keySuffix, T element, SetCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        return redisTemplate.opsForSet().remove(cacheKey, element.toString());
    }

    /**
     * 检查元素是否在Set缓存中
     */
    public <T> Boolean isMember(String keySuffix, T element, SetCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        return redisTemplate.opsForSet().isMember(cacheKey, element.toString());
    }

    /**
     * 获取Set缓存大小
     */
    public Long getSetSize(String keySuffix, SetCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        Long size = redisTemplate.opsForSet().size(cacheKey);

        // 如果大小为1且包含空值标记，则实际大小为0
        if (size != null && size == 1) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(cacheKey, EMPTY_MARKER))) {
                return 0L;
            }
        }

        return size;
    }

    /**
     * 删除Set缓存
     */
    public void evict(String keySuffix, SetCacheConfig config) {
        String cacheKey = config.getCacheKeyPrefix() + keySuffix;
        redisTemplate.delete(cacheKey);
        log.debug("删除Set缓存 [key:{}]", cacheKey);
    }
}