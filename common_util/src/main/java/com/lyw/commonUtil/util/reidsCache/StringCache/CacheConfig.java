package com.lyw.commonUtil.util.reidsCache.StringCache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheConfig {
    /**
     * 缓存key前缀
     */
    private String cacheKeyPrefix;

    /**
     * 锁key前缀
     */
    private String lockKeyPrefix;

    /**
     * 缓存过期时间
     */
    private Duration cacheTimeout;

    /**
     * 空值缓存过期时间（防穿透）
     */
    private Duration nullValueTimeout = Duration.ofMinutes(5);

    /**
     * 锁等待时间（秒）
     */
    private long lockWaitTime = 3;

    /**
     * 锁持有时间（秒）
     */
    private long lockLeaseTime = 30;

    /**
     * 是否缓存空值（防穿透）
     */
    private boolean cacheNullValue = true;
    /**
     * 时间单位（默认毫秒）
     */
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    public CacheConfig(String cacheKeyPrefix, String lockKeyPrefix, Duration cacheTimeout) {
        this.cacheKeyPrefix = cacheKeyPrefix;
        this.lockKeyPrefix = lockKeyPrefix;
        this.cacheTimeout = cacheTimeout;
    }
}
