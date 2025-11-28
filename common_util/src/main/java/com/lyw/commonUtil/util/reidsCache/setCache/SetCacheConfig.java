package com.lyw.commonUtil.util.reidsCache.setCache;

import lombok.Data;
import lombok.experimental.Accessors;
import java.util.concurrent.TimeUnit;

/**
 * Set缓存配置
 */
@Data
@Accessors(chain = true)
public class SetCacheConfig {
    /**
     * 缓存键前缀
     */
    private String cacheKeyPrefix;

    /**
     * 锁键前缀
     */
    private String lockKeyPrefix;

    /**
     * 缓存超时时间（默认30分钟）
     */
    private long cacheTimeout = 30 * 60 * 1000L;

    /**
     * 空值超时时间（默认5分钟，防止缓存穿透）
     */
    private long nullValueTimeout = 5 * 60 * 1000L;

    /**
     * 锁等待时间（默认3秒）
     */
    private long lockWaitTime = 3000L;

    /**
     * 锁持有时间（默认10秒）
     */
    private long lockLeaseTime = 10000L;

    /**
     * 是否缓存空值（防止缓存穿透）
     */
    private boolean cacheNullValue = true;

    /**
     * 时间单位（默认毫秒）
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
}