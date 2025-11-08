package com.lyw.cloudInteraction.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudInteraction.constant.RedisConstant;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.mapper.InteractionsDao;
import com.lyw.cloudInteraction.mapper.ReviewRepliesDao;
import com.lyw.cloudInteraction.mapper.ReviewsDao;
import com.lyw.cloudInteraction.utils.RedisUtil;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.vo.InteractionsVo;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 点赞收藏数据同步任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InteractionSyncJob {

    private final RedisUtil redisUtil;
    private final InteractionsDao interactionsDao;
    private final FavoriteItemsDao favoriteItemsDao;
    private final ReviewsDao reviewsDao;
    private final ReviewRepliesDao reviewRepliesDao;
    private final RedisTemplate<String, Object> redisTemplate;

    // 重试配置
    private static final int MAX_RETRY_COUNT = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 互动数据同步任务
     */
    @XxlJob("interactionSyncJob")
    @Transactional(rollbackFor = Exception.class)
    public void syncInteractionData() {
        XxlJobHelper.log("开始执行互动数据同步任务");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        boolean lockAcquired = false;
        try {
            // 获取分布式锁，防止重复执行
            lockAcquired = redisTemplate.opsForValue().setIfAbsent(
                    RedisConstant.SYNC_LOCK_KEY, "locked", 10, TimeUnit.MINUTES);

            if (!lockAcquired) {
                XxlJobHelper.log("获取锁失败，任务可能正在其他实例执行");
                return;
            }

            // 检查是否在预热保护窗口内
            if (isInWarmUpProtectionWindow()) {
                XxlJobHelper.log("检测到预热保护窗口，延迟同步任务执行");
                return;
            }

            // 同步互动数据
            int interactionResult = syncInteractionDataFromRedis();

            // 同步收藏数据
            int favoriteResult = syncFavoriteDataFromRedis();

            // 同步计数数据
            int countResult = syncCountDataFromRedis();

            stopWatch.stop();
            XxlJobHelper.log("互动数据同步任务执行完成, 耗时: {}ms, 互动:{}条, 收藏:{}条, 计数:{}个",
                    stopWatch.getTotalTimeMillis(), interactionResult, favoriteResult, countResult);

        } catch (Exception e) {
            XxlJobHelper.log("互动数据同步任务执行失败: " + e.getMessage());
            log.error("互动数据同步任务执行失败", e);
            throw e;
        } finally {
            // 释放锁
            if (lockAcquired) {
                try {
                    redisTemplate.delete(RedisConstant.SYNC_LOCK_KEY);
                } catch (Exception e) {
                    log.warn("释放分布式锁失败", e);
                }
            }
        }
    }

    /**
     * 检查是否在预热保护窗口内
     */
    private boolean isInWarmUpProtectionWindow() {
        try {
            Object protection = redisUtil.hGet("warmup:protection", "current");
            return protection != null;
        } catch (Exception e) {
            log.warn("检查预热保护窗口失败", e);
            return false;
        }
    }

    /**
     * 同步互动数据
     */
    private int syncInteractionDataFromRedis() {
        Map<Object, Object> interactionData = redisUtil.hGetAll(RedisConstant.INTERACTION_HASH_KEY);

        if (interactionData == null || interactionData.isEmpty()) {
            XxlJobHelper.log("没有需要同步的互动数据");
            return 0;
        }

        List<String> processedFields = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int skipCount = 0;

        for (Map.Entry<Object, Object> entry : interactionData.entrySet()) {
            String field = (String) entry.getKey();

            try {
                Map<String, Object> data = (Map<String, Object>) entry.getValue();

                // 检查同步状态，如果是已同步的数据，跳过处理
                Integer syncStatus = (Integer) data.get("syncStatus");
                if (syncStatus != null && syncStatus == 1) {
                    // 检查是否为预热数据，如果是且超过保护期，可以删除
                    if (isWarmUpDataExpired(data)) {
                        processedFields.add(field);
                        skipCount++;
                    }
                    continue;
                }

                // 解析field获取关键信息
                String[] fieldParts = field.split(":");
                if (fieldParts.length < 4) {
                    log.warn("无效的field格式: {}", field);
                    errorCount++;
                    continue;
                }

                String targetType = fieldParts[0];
                Long targetId = Long.parseLong(fieldParts[1]);
                Long userId = Long.parseLong(fieldParts[2]);
                String interactionType = fieldParts[3];

                // 使用重试机制插入数据
                boolean inserted = insertInteractionWithRetry(targetType, targetId, userId, interactionType, data);

                if (inserted) {
                    processedFields.add(field);
                    successCount++;
                } else {
                    errorCount++;
                }

            } catch (Exception e) {
                log.error("同步互动数据失败, field: {}", field, e);
                errorCount++;
            }
        }

        // 删除已处理的数据（只删除成功处理的数据）
        for (String field : processedFields) {
            try {
                redisUtil.hDelete(RedisConstant.INTERACTION_HASH_KEY, field);
            } catch (Exception e) {
                log.warn("删除已处理field失败: {}", field, e);
            }
        }

        XxlJobHelper.log("互动数据同步完成: 成功={}, 失败={}, 跳过={}", successCount, errorCount, skipCount);
        return successCount;
    }

    /**
     * 检查预热数据是否过期（可删除）
     */
    private boolean isWarmUpDataExpired(Map<String, Object> data) {
        try {
            String warmUpTimeStr = (String) data.get("warmUpTime");
            if (warmUpTimeStr != null) {
                LocalDateTime warmUpTime = LocalDateTime.parse(warmUpTimeStr);
                // 预热数据超过2小时可以删除
                return warmUpTime.isBefore(LocalDateTime.now().minusHours(2));
            }
        } catch (Exception e) {
            log.warn("检查预热数据过期时间失败", e);
        }
        return false;
    }

    /**
     * 带重试的插入互动数据
     */
    private boolean insertInteractionWithRetry(String targetType, Long targetId, Long userId,
                                               String interactionType, Map<String, Object> data) {
        for (int i = 0; i < MAX_RETRY_COUNT; i++) {
            try {
                // 检查是否已经存在
                Integer exists = interactionsDao.selectCount(new LambdaQueryWrapper<InteractionsVo>()
                        .eq(InteractionsVo::getUserId, userId)
                        .eq(InteractionsVo::getTargetType, targetType)
                        .eq(InteractionsVo::getTargetId, targetId)
                        .eq(InteractionsVo::getInteractionType, interactionType));

                if (exists > 0) {
                    return true; // 已存在，视为成功
                }

                // 插入数据库
                InteractionsVo interactionsVo = new InteractionsVo();
                interactionsVo.setUserId(userId)
                        .setTargetType(targetType)
                        .setTargetId(targetId)
                        .setInteractionType(interactionType)
                        .setCru((String) data.getOrDefault("cru", CurUserUtil.getUserCode()))
                        .setLuu((String) data.getOrDefault("luu", CurUserUtil.getUserCode()))
                        .setCrd((String) data.getOrDefault("crd", DateTimeUtils.getCurrentDateTime()))
                        .setLud((String) data.getOrDefault("lud", DateTimeUtils.getCurrentDateTime()));

                return interactionsDao.insert(interactionsVo) > 0;

            } catch (Exception e) {
                log.warn("第{}次插入互动数据失败: {}/{}/{}/{}", i + 1, targetType, targetId, userId, interactionType, e);
                if (i < MAX_RETRY_COUNT - 1) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 同步收藏数据
     */
    private int syncFavoriteDataFromRedis() {
        Map<Object, Object> favoriteItemData = redisUtil.hGetAll(RedisConstant.FAVORITE_ITEM_HASH_KEY);

        if (favoriteItemData == null || favoriteItemData.isEmpty()) {
            XxlJobHelper.log("没有需要同步的收藏数据");
            return 0;
        }

        List<String> processedFields = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (Map.Entry<Object, Object> entry : favoriteItemData.entrySet()) {
            try {
                String field = (String) entry.getKey();
                Map<String, Object> data = (Map<String, Object>) entry.getValue();

                // 检查同步状态
                Integer syncStatus = (Integer) data.get("syncStatus");
                if (syncStatus != null && syncStatus == 1) {
                    if (isWarmUpDataExpired(data)) {
                        processedFields.add(field);
                    }
                    continue;
                }

                // 解析field获取关键信息
                String[] fieldParts = field.split(":");
                Long favoriteId = Long.parseLong(fieldParts[1]);
                String targetType = fieldParts[2];
                Long targetId = Long.parseLong(fieldParts[3]);

                // 检查是否已经存在
                Integer exists = favoriteItemsDao.selectCount(new LambdaQueryWrapper<FavoriteItemsVo>()
                        .eq(FavoriteItemsVo::getFavoriteId, favoriteId)
                        .eq(FavoriteItemsVo::getTargetType, targetType)
                        .eq(FavoriteItemsVo::getTargetId, targetId));

                if (exists == 0) {
                    // 插入数据库
                    FavoriteItemsVo favoriteItem = new FavoriteItemsVo();
                    favoriteItem.setFavoriteId(favoriteId)
                            .setTargetType(targetType)
                            .setTargetId(targetId)
                            .setNotes((String) data.get("notes"))
                            .setCru((String) data.getOrDefault("cru", CurUserUtil.getUserCode()))
                            .setLuu((String) data.getOrDefault("luu", CurUserUtil.getUserCode()))
                            .setCrd((String) data.getOrDefault("crd", DateTimeUtils.getCurrentDateTime()))
                            .setLud((String) data.getOrDefault("lud", DateTimeUtils.getCurrentDateTime()));

                    favoriteItemsDao.insert(favoriteItem);
                }

                processedFields.add(field);
                successCount++;

            } catch (Exception e) {
                log.error("同步收藏数据失败, field: {}", entry.getKey(), e);
                errorCount++;
            }
        }

        // 删除已处理的数据
        for (String field : processedFields) {
            redisUtil.hDelete(RedisConstant.FAVORITE_ITEM_HASH_KEY, field);
        }

        XxlJobHelper.log("收藏数据同步完成: 成功={}, 失败={}", successCount, errorCount);
        return successCount;
    }

    /**
     * 同步计数数据 - 改为增量同步
     */
    private int syncCountDataFromRedis() {
        int totalCount = 0;

        // 同步评价点赞计数
        totalCount += syncReviewLikeCount();

        // 同步评价有用计数
        totalCount += syncReviewUsefulCount();

        // 同步回复点赞计数
        totalCount += syncReplyLikeCount();

        // 同步收藏夹项目计数
        totalCount += syncFavoriteItemCount();

        return totalCount;
    }

    private int syncReviewLikeCount() {
        Map<Object, Object> likeCounts = redisUtil.hGetAll("review:like:count");
        if (likeCounts == null) return 0;

        int count = 0;
        for (Map.Entry<Object, Object> entry : likeCounts.entrySet()) {
            try {
                Long reviewId = Long.parseLong((String) entry.getKey());
                Long redisCount = Long.parseLong(entry.getValue().toString());

                // 获取数据库当前计数
                Long dbCount = reviewsDao.getLikeCount(reviewId);
                if (dbCount == null) dbCount = 0L;

                // 使用较大值（防止数据丢失）
                Long finalCount = Math.max(redisCount, dbCount);

                reviewsDao.updateLikeCount(reviewId, finalCount);
                count++;
            } catch (Exception e) {
                log.error("同步评价点赞计数失败: {}", entry.getKey(), e);
            }
        }
        return count;
    }

    private int syncReviewUsefulCount() {
        Map<Object, Object> usefulCounts = redisUtil.hGetAll("review:useful:count");
        if (usefulCounts == null) return 0;

        int count = 0;
        for (Map.Entry<Object, Object> entry : usefulCounts.entrySet()) {
            try {
                Long reviewId = Long.parseLong((String) entry.getKey());
                Long redisCount = Long.parseLong(entry.getValue().toString());

                Long dbCount = reviewsDao.getUsefulCount(reviewId);
                if (dbCount == null) dbCount = 0L;

                Long finalCount = Math.max(redisCount, dbCount);
                reviewsDao.updateUsefulCount(reviewId, finalCount);
                count++;
            } catch (Exception e) {
                log.error("同步评价有用计数失败: {}", entry.getKey(), e);
            }
        }
        return count;
    }

    private int syncReplyLikeCount() {
        Map<Object, Object> likeCounts = redisUtil.hGetAll("reply:like:count");
        if (likeCounts == null) return 0;

        int count = 0;
        for (Map.Entry<Object, Object> entry : likeCounts.entrySet()) {
            try {
                Long replyId = Long.parseLong((String) entry.getKey());
                Long redisCount = Long.parseLong(entry.getValue().toString());

                Long dbCount = reviewRepliesDao.getLikeCount(replyId);
                if (dbCount == null) dbCount = 0L;

                Long finalCount = Math.max(redisCount, dbCount);
                reviewRepliesDao.updateLikeCount(replyId, finalCount);
                count++;
            } catch (Exception e) {
                log.error("同步回复点赞计数失败: {}", entry.getKey(), e);
            }
        }
        return count;
    }

    private int syncFavoriteItemCount() {
        Map<Object, Object> itemCounts = redisUtil.hGetAll("favorite:item:count");
        if (itemCounts == null) return 0;

        int count = 0;
        for (Map.Entry<Object, Object> entry : itemCounts.entrySet()) {
            try {
                Long favoriteId = Long.parseLong((String) entry.getKey());
                Long redisCount = Long.parseLong(entry.getValue().toString());

                // 查询实际的项目数量作为基准
                long actualCount = favoriteItemsDao.selectCount(
                        new LambdaQueryWrapper<FavoriteItemsVo>()
                                .eq(FavoriteItemsVo::getFavoriteId, favoriteId));

                // 使用较大值
                Long finalCount = Math.max(redisCount, actualCount);

                updateFavoriteItemCount(favoriteId, finalCount);
                count++;
            } catch (Exception e) {
                log.error("同步收藏计数失败: {}", entry.getKey(), e);
            }
        }
        return count;
    }

    /**
     * 更新收藏夹项目计数
     */
    private void updateFavoriteItemCount(Long favoriteId, Long count) {
        try {
            // 这里需要调用FavoritesDao的update方法
            // 示例：favoritesDao.updateItemCount(favoriteId, count);
            log.info("更新收藏夹计数: favoriteId={}, count={}", favoriteId, count);
        } catch (Exception e) {
            log.error("更新收藏夹计数失败: {}", favoriteId, e);
        }
    }
}