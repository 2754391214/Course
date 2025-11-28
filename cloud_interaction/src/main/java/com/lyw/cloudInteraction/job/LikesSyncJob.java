package com.lyw.cloudInteraction.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyw.cloudInteraction.mapper.LikesDao;
import com.lyw.cloudInteraction.vo.LikesVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.util.RedisUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 点赞收藏数据同步任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LikesSyncJob {
    private final RedisUtils redisUtils;
    private final LikesDao likesDao;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 互动数据同步任务
     */
    @XxlJob("likeSyncJob")
    @Transactional(rollbackFor = Exception.class)
    public void syncLikesData() {
        XxlJobHelper.log("开始执行点赞同步任务");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        boolean lockAcquired = false;
        try {
            // 获取分布式锁，防止重复执行
            lockAcquired = redisTemplate.opsForValue().setIfAbsent(RedisKeyConstant.LOCK_SYNC_SAVE_LIKE, "locked", 10, TimeUnit.MINUTES);

            if (!lockAcquired) {
                XxlJobHelper.log("获取点赞锁失败，任务可能正在其他实例执行");
                return;
            }

            // 同步收藏数据
            int favoriteResult = syncFavoriteDataFromRedis();

            stopWatch.stop();
            XxlJobHelper.log("点赞数据同步任务执行完成, 耗时: {}ms, 点赞:{}条", stopWatch.getTotalTimeMillis(), favoriteResult);

        } catch (Exception e) {
            XxlJobHelper.log("点赞数据同步任务执行失败: " + e.getMessage());
            log.error("点赞数据同步任务执行失败", e);
            throw e;
        } finally {
            // 释放锁
            if (lockAcquired) {
                try {
                    redisTemplate.delete(RedisKeyConstant.LOCK_SYNC_SAVE_LIKE);
                } catch (Exception e) {
                    log.warn("释放点赞分布式锁失败", e);
                }
            }
        }
    }

    /**
     * 同步点赞数据 - 根据Service层的Redis结构修改
     */
    private int syncFavoriteDataFromRedis() {
        Map<String, String> likesData = redisUtils.hGetAll(RedisKeyConstant.SYNC_SAVE_LIKE);

        if (CollectionUtil.isEmpty(likesData)) {
            XxlJobHelper.log("没有需要同步的点赞数据");
            return 0;
        }

        List<String> processedFields = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;
        int skipCount = 0;

        for (Map.Entry<String, String> entry : likesData.entrySet()) {
            String field = entry.getKey();
            String data = entry.getValue();

            try {
                // 解析field获取关键信息 - 格式: FAVORITE_ITEM:{targetId}:{targetType}:{userId}
                String[] fieldParts = field.split(":");
                if (fieldParts.length < 4) {
                    log.warn("无效的field格式: {}", field);
                    errorCount++;
                    continue;
                }

                Long targetId = Long.parseLong(fieldParts[1]);
                String targetType = fieldParts[2];
                String userId = fieldParts[3];

                // 根据操作类型处理数据
                boolean processed = processFavoriteItem(targetId, targetType, userId, data, field);

                if (processed) {
                    processedFields.add(field);
                    successCount++;
                    XxlJobHelper.log("成功处理收藏数据: targetId={}, targetType={}, userId={}", targetId, targetType, userId);
                } else {
                    errorCount++;
                }

            } catch (Exception e) {
                log.error("同步收藏数据失败, field: {}", field, e);
                errorCount++;
            }
        }

        // 删除已处理的数据
        if (CollectionUtil.isNotEmpty(processedFields)) {
            for (String field : processedFields) {
                try {
                    redisUtils.hDelete(RedisKeyConstant.SYNC_SAVE_LIKE, field);
                    log.debug("删除已处理收藏数据: {}", field);
                } catch (Exception e) {
                    log.warn("删除已处理field失败: {}", field, e);
                }
            }
        }

        XxlJobHelper.log("收藏数据同步完成: 成功={}, 失败={}, 跳过={}", successCount, errorCount, skipCount);
        return successCount;
    }

    /**
     * 处理收藏项数据
     */
    private boolean processFavoriteItem(Long targetId, String targetType, String userId, String data, String field) {
        // 检查是否取消收藏
        boolean result;
        if (StrUtil.equals(data,"0")) {
            result = handleCancelLike(targetId, targetType, userId);
        } else {
            result = handleAddLike(targetId, targetType, userId);
        }
        redisUtils.hDelete(RedisKeyConstant.SYNC_SAVE_LIKE,field);
        return result;
    }

    /**
     * 处理取消收藏操作
     */
    private boolean handleCancelLike(Long targetId, String targetType, String userId) {
        try {
            int result = likesDao.delete(
                    new LambdaQueryWrapper<LikesVo>()
                            .eq(LikesVo::getTargetId,targetId)
                            .eq(LikesVo::getTargetType,targetType)
                            .eq(LikesVo::getUserId, userId));
            return result > 0;
        } catch (Exception e) {
            log.error("处理取消收藏失败: targetId={}, targetType={}, userId={}", targetId, targetType, userId, e);
            return false;
        }
    }

    /**
     * 处理添加收藏操作
     */
    private boolean handleAddLike(Long targetId, String targetType, String userId) {
        try {
            // 检查是否已存在
            LambdaQueryWrapper<LikesVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LikesVo::getTargetId, targetId)
                    .eq(LikesVo::getTargetType, targetType)
                    .eq(LikesVo::getUserId, userId);
            LikesVo existingItem = likesDao.selectOne(queryWrapper);

            LikesVo data = new LikesVo();
            data.setUserId(Long.valueOf(userId));
            data.setTargetType(targetType);
            data.setTargetId(targetId);
            if (existingItem != null) {
                int result = likesDao.updateById(data);
                return result > 0;
            } else {
                // 新增收藏记录
                int result = likesDao.insert(data);
                return result > 0;
            }
        } catch (Exception e) {
            log.error("处理添加收藏失败: targetId={}, targetType={}, userId={}", targetId, targetType, userId, e);
            return false;
        }
    }
}