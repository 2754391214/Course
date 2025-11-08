package com.lyw.cloudInteraction.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudInteraction.constant.RedisConstant;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.mapper.InteractionsDao;
import com.lyw.cloudInteraction.utils.RedisUtil;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.cloudInteraction.vo.InteractionsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWarmUpService {

    private final InteractionsDao interactionsDao;
    private final FavoriteItemsDao favoriteItemsDao;
    private final RedisUtil redisUtil;

    // 预热保护时间窗口（分钟）
    private static final int PROTECTION_WINDOW_MINUTES = 10;
    // 批次大小
    private static final int BATCH_SIZE = 1000;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpRedisData() {
        log.info("开始预热Redis数据...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 设置预热保护窗口
            setWarmUpProtectionWindow();

            // 获取保护时间点
            LocalDateTime protectionTime = LocalDateTime.now().minusMinutes(PROTECTION_WINDOW_MINUTES);

            // 预热互动数据（只预热较旧的数据）
            warmUpInteractions(protectionTime);

            // 预热收藏数据（只预热较旧的数据）
            warmUpFavorites(protectionTime);

            // 预热计数数据
            warmUpCounts();

            stopWatch.stop();
            log.info("Redis数据预热完成，耗时: {} 毫秒", stopWatch.getTotalTimeMillis());

        } catch (Exception e) {
            log.error("Redis数据预热失败", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    /**
     * 设置预热保护窗口，标记预热开始时间
     */
    private void setWarmUpProtectionWindow() {
        try {
            Map<String, Object> protectionData = new HashMap<>();
            protectionData.put("startTime", LocalDateTime.now().toString());
            protectionData.put("protectionWindow", PROTECTION_WINDOW_MINUTES);
            redisUtil.hSet("warmup:protection", "current", protectionData);
            // 保护窗口30分钟
            redisUtil.expire("warmup:protection", 30, java.util.concurrent.TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("设置预热保护窗口失败", e);
        }
    }

    /**
     * 预热互动数据 - 只预热保护时间点之前的数据
     */
    private void warmUpInteractions(LocalDateTime protectionTime) {
        try {
            // 只查询在保护时间点之前创建的数据
            List<InteractionsVo> interactions = interactionsDao.selectList(
                    new LambdaQueryWrapper<InteractionsVo>()
                            .lt(InteractionsVo::getCrd, protectionTime)
            );

            int successCount = 0;
            int skipCount = 0;

            for (InteractionsVo interaction : interactions) {
                String field = RedisConstant.generateInteractionField(
                        interaction.getTargetType(),
                        interaction.getTargetId(),
                        interaction.getUserId(),
                        interaction.getInteractionType()
                );

                // 关键：如果Redis中已存在该field，说明可能是宕机期间的新数据，跳过
                if (redisUtil.hExists(RedisConstant.INTERACTION_HASH_KEY, field)) {
                    skipCount++;
                    continue;
                }

                Map<String, Object> interactionData = new HashMap<>();
                interactionData.put("userId", interaction.getUserId());
                interactionData.put("targetType", interaction.getTargetType());
                interactionData.put("targetId", interaction.getTargetId());
                interactionData.put("interactionType", interaction.getInteractionType());
                interactionData.put("cru", interaction.getCru());
                interactionData.put("luu", interaction.getLuu());
                interactionData.put("crd", interaction.getCrd());
                interactionData.put("lud", interaction.getLud());
                interactionData.put("syncStatus", 1); // 已同步
                interactionData.put("warmUpTime", LocalDateTime.now().toString()); // 标记为预热数据

                redisUtil.hSet(RedisConstant.INTERACTION_HASH_KEY, field, interactionData);
                successCount++;

                // 分批处理，避免内存和性能问题
                if (successCount % BATCH_SIZE == 0) {
                    log.info("已预热互动数据: {} 条", successCount);
                }
            }

            log.info("互动数据预热完成: 成功={}, 跳过={}, 总计={}", successCount, skipCount, interactions.size());

        } catch (Exception e) {
            log.error("预热互动数据失败", e);
        }
    }

    /**
     * 预热收藏数据 - 只预热保护时间点之前的数据
     */
    private void warmUpFavorites(LocalDateTime protectionTime) {
        try {
            // 只查询在保护时间点之前创建的数据
            List<FavoriteItemsVo> favoriteItems = favoriteItemsDao.selectList(
                    new LambdaQueryWrapper<FavoriteItemsVo>()
                            .lt(FavoriteItemsVo::getCrd, protectionTime)
            );

            int successCount = 0;
            int skipCount = 0;

            for (FavoriteItemsVo item : favoriteItems) {
                String field = RedisConstant.generateFavoriteItemField(
                        item.getFavoriteId(),
                        item.getTargetType(),
                        item.getTargetId()
                );

                // 检查是否已存在
                if (redisUtil.hExists(RedisConstant.FAVORITE_ITEM_HASH_KEY, field)) {
                    skipCount++;
                    continue;
                }

                Map<String, Object> favoriteData = new HashMap<>();
                favoriteData.put("favoriteId", item.getFavoriteId());
                favoriteData.put("targetType", item.getTargetType());
                favoriteData.put("targetId", item.getTargetId());
                favoriteData.put("notes", item.getNotes());
                favoriteData.put("cru", item.getCru());
                favoriteData.put("luu", item.getLuu());
                favoriteData.put("crd", item.getCrd());
                favoriteData.put("lud", item.getLud());
                favoriteData.put("syncStatus", 1);
                favoriteData.put("warmUpTime", LocalDateTime.now().toString());

                redisUtil.hSet(RedisConstant.FAVORITE_ITEM_HASH_KEY, field, favoriteData);
                successCount++;

                if (successCount % BATCH_SIZE == 0) {
                    log.info("已预热收藏数据: {} 条", successCount);
                }
            }

            log.info("收藏数据预热完成: 成功={}, 跳过={}, 总计={}", successCount, skipCount, favoriteItems.size());

        } catch (Exception e) {
            log.error("预热收藏数据失败", e);
        }
    }

    /**
     * 预热计数数据 - 从数据库统计
     */
    private void warmUpCounts() {
        try {
            // 预热评价点赞计数
            warmUpReviewLikeCounts();

            // 预热评价有用计数
            warmUpReviewUsefulCounts();

            // 预热回复点赞计数
            warmUpReplyLikeCounts();

            // 预热收藏计数
            warmUpFavoriteCounts();

            log.info("计数数据预热完成");

        } catch (Exception e) {
            log.error("预热计数数据失败", e);
        }
    }

    private void warmUpReviewLikeCounts() {
        try {
            List<InteractionsVo> reviewLikeCounts = interactionsDao.getReviewLikeCounts();
            for (InteractionsVo count : reviewLikeCounts) {
                Long reviewId = count.getTargetId();
                Long likeCount = count.getLikeCount();
                // 不覆盖已存在的计数
                if (!redisUtil.hExists(RedisConstant.REVIEW_LIKE_COUNT_KEY, reviewId.toString())) {
                    redisUtil.hSet(RedisConstant.REVIEW_LIKE_COUNT_KEY, reviewId.toString(), likeCount);
                }
            }
            log.info("预热评价点赞计数: {} 条", reviewLikeCounts.size());
        } catch (Exception e) {
            log.error("预热评价点赞计数失败", e);
        }
    }

    private void warmUpReviewUsefulCounts() {
        try {
            List<InteractionsVo> reviewUsefulCounts = interactionsDao.getReviewUsefulCounts();
            for (InteractionsVo count : reviewUsefulCounts) {
                Long reviewId = count.getTargetId();
                Long usefulCount = count.getUsefulCount();
                if (!redisUtil.hExists(RedisConstant.REVIEW_USEFUL_COUNT_KEY, reviewId.toString())) {
                    redisUtil.hSet(RedisConstant.REVIEW_USEFUL_COUNT_KEY, reviewId.toString(), usefulCount);
                }
            }
            log.info("预热评价有用计数: {} 条", reviewUsefulCounts.size());
        } catch (Exception e) {
            log.error("预热评价有用计数失败", e);
        }
    }

    private void warmUpReplyLikeCounts() {
        try {
            List<InteractionsVo> replyLikeCounts = interactionsDao.getReplyLikeCounts();
            for (InteractionsVo count : replyLikeCounts) {
                Long replyId = count.getTargetId();
                Long likeCount = count.getLikeCount();
                if (!redisUtil.hExists(RedisConstant.REPLY_LIKE_COUNT_KEY, replyId.toString())) {
                    redisUtil.hSet(RedisConstant.REPLY_LIKE_COUNT_KEY, replyId.toString(), likeCount);
                }
            }
            log.info("预热回复点赞计数: {} 条", replyLikeCounts.size());
        } catch (Exception e) {
            log.error("预热回复点赞计数失败", e);
        }
    }

    private void warmUpFavoriteCounts() {
        try {
            // 预热收藏夹项目计数（需要新增Mapper方法）
            List<Map<String, Object>> favoriteItemCounts = favoriteItemsDao.getFavoriteItemCounts();
            for (Map<String, Object> count : favoriteItemCounts) {
                Long favoriteId = (Long) count.get("favoriteId");
                Long itemCount = (Long) count.get("itemCount");
                if (!redisUtil.hExists(RedisConstant.FAVORITE_ITEM_COUNT_KEY, favoriteId.toString())) {
                    redisUtil.hSet(RedisConstant.FAVORITE_ITEM_COUNT_KEY, favoriteId.toString(), itemCount);
                }
            }
            log.info("预热收藏计数: {} 条", favoriteItemCounts.size());
        } catch (Exception e) {
            log.error("预热收藏计数失败", e);
        }
    }

    /**
     * 手动触发预热（用于管理界面）
     */
    public void manualWarmUp() {
        new Thread(() -> {
            try {
                warmUpRedisData();
            } catch (Exception e) {
                log.error("手动预热失败", e);
            }
        }).start();
    }
}