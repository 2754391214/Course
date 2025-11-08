package com.lyw.cloudInteraction.constant;

public class RedisConstant {

    // 互动数据Hash Key
    public static final String INTERACTION_HASH_KEY = "interaction:hash";

    // 收藏数据Hash Key
    public static final String FAVORITE_HASH_KEY = "favorite:hash";
    // 收藏项数据Hash Key
    public static final String FAVORITE_ITEM_HASH_KEY = "favorite_item:hash";

    public static final String REVIEW_USEFUL_COUNT_KEY = "review:useful:count";
    public static final String REVIEW_LIKE_COUNT_KEY = "review:like:count";
    public static final String REPLY_LIKE_COUNT_KEY = "reply:like:count";
    public static final String FAVORITE_ITEM_COUNT_KEY = "favorite:item:count";

    // 锁Key
    public static final String SYNC_LOCK_KEY = "sync:lock";

    // 生成Hash Field的工具方法
    public static String generateInteractionField(String targetType, Long targetId, Long userId, String interactionType) {
        return String.format("%s:%d:%d:%s", targetType, targetId, userId, interactionType);
    }

    public static String generateFavoriteField(Long favoriteId) {
        return String.format("favorite:%d", favoriteId);
    }

    public static String generateFavoriteItemField(Long favoriteId, String targetType, Long targetId) {
        return String.format("item:%d:%s:%d", favoriteId, targetType, targetId);
    }
}