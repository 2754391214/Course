package com.lyw.commonUtil.util.reidsCache;

/**
 * 缓存数据加载器
 */
@FunctionalInterface
public interface CacheLoader<T> {
    /**
     * 加载数据
     */
    T load() throws Exception;

    /**
     * 默认的空值处理器
     */
    default T handleNull() {
        return null;
    }
}
