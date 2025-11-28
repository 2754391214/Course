package com.lyw.commonUtil.util.reidsCache.setCache;

import java.util.Set;

/**
 * Set缓存数据加载器
 */
@FunctionalInterface
public interface SetCacheLoader<T> {
    /**
     * 加载数据
     */
    Set<T> load();

    /**
     * 处理空值情况（默认返回空Set）
     */
    default Set<T> handleNull() {
        return java.util.Collections.emptySet();
    }
}