package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.dto.FavoriteItemsDto;
import com.lyw.cloudInteraction.mapper.FavoriteItemsDao;
import com.lyw.cloudInteraction.service.FavoriteItemsBo;
import com.lyw.cloudInteraction.service.HeatEventPublisher;
import com.lyw.cloudInteraction.vo.FavoriteItemsVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.BeanConverter;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.reidsCache.setCache.DistributedSetCacheHelper;
import com.lyw.commonUtil.util.reidsCache.setCache.SetCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * <p>
 * 收藏项表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class FavoriteItemsImpl extends ServiceImpl<FavoriteItemsDao, FavoriteItemsVo> implements FavoriteItemsBo {
    @Resource
    private FavoriteItemsDao favoriteItemsDao;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private DistributedSetCacheHelper distributedSetCacheHelper;
    @Resource
    private HeatEventPublisher heatEventPublisher;
    @Override
    public CourseResponseWrapper addFavoriteItem(FavoriteItemsDto dto) {
        Long targetId = dto.getTargetId();
        String targetType = dto.getTargetType();
        String userId = CurUserUtil.getUserId();
        String targetFavoriteKey = String.format(RedisKeyConstant.FAVORITE_ITEM, targetId, targetType);

        FavoriteItemsVo favoriteItemsVo = BeanConverter.dtoToVo(dto,FavoriteItemsVo.class);
        favoriteItemsVo.setCannelFavorite(false);
        favoriteItemsVo.setCruAndLuu(userId);
        favoriteItemsVo.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        redisUtils.hPut(RedisKeyConstant.SYNC_SAVE_FAVORITE,targetFavoriteKey+":"+userId,favoriteItemsVo);

        // 使用 Set 记录用户收藏关系
        Long result = redisUtils.sAdd(targetFavoriteKey, userId);
        if (ObjectUtil.isNotEmpty(dto.getCourseId())) {
            heatEventPublisher.publishEvent(targetType,dto.getCourseId(), CommonKeyConstant.FAVORITE,null);
        }

        if (result > 0) {
            // 更新排行榜：增加收藏数 TODO 异步给排行榜
            String rankKey = String.format(RedisKeyConstant.FAVORITE_ITEM_COUNT, targetType);
            redisUtils.zIncrementScore(rankKey,  targetId.toString(),1);
        }
        return CourseResponseWrapper.getSuccess(result > 0 ? "收藏成功" : "已收藏");
    }

    @Override
    public CourseResponseWrapper removeFavoriteItem(FavoriteItemsDto dto) {
        Long targetId = dto.getTargetId();
        String targetType = dto.getTargetType();
        String userId = CurUserUtil.getUserId();
        String targetFavoriteKey = String.format(RedisKeyConstant.FAVORITE_ITEM, targetId, targetType);

        FavoriteItemsVo favoriteItemsVo = new FavoriteItemsVo();
        favoriteItemsVo.setTargetId(targetId);
        favoriteItemsVo.setTargetType(targetType);
        favoriteItemsVo.setCannelFavorite(true);
        redisUtils.hPut(RedisKeyConstant.SYNC_SAVE_FAVORITE,targetFavoriteKey+":"+userId,favoriteItemsVo);

        // 使用 Set 移除用户收藏关系
        Long result = redisUtils.sRemove(targetFavoriteKey, userId);

        if (result > 0) {
            // 更新排行榜：减少收藏数 TODO 异步给排行榜
            String rankKey = String.format(RedisKeyConstant.FAVORITE_ITEM_COUNT, targetType);
            redisUtils.zIncrementScore(rankKey,targetId.toString(),-1);
        }
        return CourseResponseWrapper.getSuccess(result > 0 ? "取消收藏成功" : "未收藏");
    }
    @Override
    public CourseResponseWrapper getFavoriteStatus(String targetType, Long targetId, Long userId) {
        String targetFavoriteKey = String.format(RedisKeyConstant.FAVORITE_ITEM, targetId, targetType);
        String lockTargetFavoriteKey = String.format(RedisKeyConstant.LOCK_FAVORITE_ITEM, targetId, targetType);
        Set<Long> targetCache = distributedSetCacheHelper.getOrLoad(
                "",
                new SetCacheConfig()
                        .setCacheKeyPrefix(targetFavoriteKey)
                        .setLockKeyPrefix(lockTargetFavoriteKey)
                        .setCacheTimeout(30 * 60 * 1000L) // 30分钟
                        .setNullValueTimeout(5 * 60 * 1000L) // 5分钟
                        .setLockWaitTime(3000L)
                        .setLockLeaseTime(10000L),
                () -> {
                    // 数据加载逻辑 - 从数据库查询并转换为Set
                    return new HashSet<>(baseMapper.selectUserId(targetType, targetId));
                },
                Long.class
        );
        Map<String, Object> result = Collections.unmodifiableMap(
                new HashMap<>() {{
                    put("isFavorited", targetCache.contains(userId));
                    put("favoritedCount", targetCache.size());
                }}
        );
        return CourseResponseWrapper.getSuccess(result);
    }

    @Override
    public CourseResponseWrapper getItemsByTarget(String targetType, Long targetId) {
        return CourseResponseWrapper.getSuccess(favoriteItemsDao.selectList(
                new LambdaQueryWrapper<FavoriteItemsVo>()
                        .eq(FavoriteItemsVo::getTargetType, targetType)
                        .eq(FavoriteItemsVo::getTargetId, targetId)
                        .orderByDesc(FavoriteItemsVo::getCrd)));
    }

    public CourseResponseWrapper getFavoriteItems(Long favoriteId) {
        // 并行执行数据库查询和Redis查询
        Long userId = Long.valueOf(CurUserUtil.getUserId());
        CompletableFuture<List<FavoriteItemsVo>> dbFuture = CompletableFuture.supplyAsync(() ->
                favoriteItemsDao.selectList(
                        new LambdaQueryWrapper<FavoriteItemsVo>()
                                .eq(FavoriteItemsVo::getUserId, userId)
                                .eq(FavoriteItemsVo::getFavoriteId, favoriteId)
                ));

        CompletableFuture<Map<String, FavoriteItemsVo>> redisFuture = CompletableFuture.supplyAsync(() ->
                redisUtils.hGetAll(RedisKeyConstant.SYNC_SAVE_FAVORITE));

        try {
            // 等待两个任务都完成，然后合并结果
            return dbFuture.thenCombine(redisFuture, (dbFavorites, pendingData) ->
                            mergeFavoriteData(userId, dbFavorites, pendingData))  // 传递userId
                    .thenApply(CourseResponseWrapper::getSuccess)
                    .get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.warn("Parallel query failed, fallback to sequential query", e);
            return fallbackSequentialQuery(userId, favoriteId);
        }
    }

    /**
     * 合并数据库和Redis的收藏数据
     */
    private List<FavoriteItemsVo> mergeFavoriteData(Long userId, List<FavoriteItemsVo> dbFavorites, Map<String, FavoriteItemsVo> pendingData) {
        if (CollectionUtils.isEmpty(dbFavorites)) {
            dbFavorites = new ArrayList<>();
        }

        if (CollectionUtils.isEmpty(pendingData)) {
            return dbFavorites;
        }

        // 过滤出当前用户的待同步数据，传递userId
        List<FavoriteItemsVo> userPendingData = extractUserPendingData(userId, pendingData);

        if (CollectionUtils.isEmpty(userPendingData)) {
            return dbFavorites;
        }

        // 分离删除和新增操作
        Map<Boolean, List<FavoriteItemsVo>> partitionedData = userPendingData.stream()
                .collect(Collectors.partitioningBy(FavoriteItemsVo::getCannelFavorite));

        List<FavoriteItemsVo> deleteData = partitionedData.get(true);
        List<FavoriteItemsVo> addData = partitionedData.get(false);

        // 创建结果集合
        List<FavoriteItemsVo> result = new ArrayList<>(dbFavorites);

        // 处理删除操作
        if (CollectionUtils.isNotEmpty(deleteData)) {
            Set<String> deleteKeys = deleteData.stream()
                    .map(this::generateItemKey)
                    .collect(Collectors.toSet());

            result = result.stream()
                    .filter(dbItem -> !deleteKeys.contains(generateItemKey(dbItem)))
                    .collect(Collectors.toList());
        }

        // 处理新增操作
        if (CollectionUtils.isNotEmpty(addData)) {
            Set<String> existingKeys = result.stream()
                    .map(this::generateItemKey)
                    .collect(Collectors.toSet());

            List<FavoriteItemsVo> uniqueAddData = addData.stream()
                    .filter(addItem -> !existingKeys.contains(generateItemKey(addItem)))
                    .collect(Collectors.toList());

            result.addAll(uniqueAddData);
        }

        return result;
    }

    /**
     * 提取用户的待同步数据
     */
    private List<FavoriteItemsVo> extractUserPendingData(Long userId, Map<String, FavoriteItemsVo> pendingData) {
        return pendingData.entrySet().stream()
                .filter(entry -> {
                    String[] keyParts = entry.getKey().split(":");
                    return keyParts.length > 3 && String.valueOf(userId).equals(keyParts[3]);
                })
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    /**
     * 生成收藏项的唯一键
     */
    private String generateItemKey(FavoriteItemsVo item) {
        return item.getTargetId() + ":" + item.getTargetType();
    }

    /**
     * 降级方案：串行查询
     */
    private CourseResponseWrapper fallbackSequentialQuery(Long userId, Long favoriteId) {
        List<FavoriteItemsVo> dbFavorites = favoriteItemsDao.selectList(
                new LambdaQueryWrapper<FavoriteItemsVo>()
                        .eq(FavoriteItemsVo::getUserId, userId)
                        .eq(FavoriteItemsVo::getFavoriteId, favoriteId)
        );

        if (CollectionUtils.isEmpty(dbFavorites)) {
            dbFavorites = new ArrayList<>();
        }

        Map<String, FavoriteItemsVo> pendingData = redisUtils.hGetAll(RedisKeyConstant.SYNC_SAVE_FAVORITE);
        if (CollectionUtils.isNotEmpty(pendingData)) {
            List<FavoriteItemsVo> mergedData = mergeFavoriteData(userId, dbFavorites, pendingData);
            return CourseResponseWrapper.getSuccess(mergedData);
        }

        return CourseResponseWrapper.getSuccess(dbFavorites);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper batchRemoveFavoriteItems(List<FavoriteItemsDto> dtos) {
        dtos.stream().forEach(item->removeFavoriteItem(item));
        return CourseResponseWrapper.getSuccess("批量移除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper moveFavoriteItem(FavoriteItemsDto dto) {
        FavoriteItemsVo favoriteItemsVo = BeanConverter.dtoToVo(dto,FavoriteItemsVo.class);
        String userId = CurUserUtil.getUserId();
        String currentDateTime = DateTimeUtils.getCurrentDateTime();
        //沒有ID代表它是从redis中获取的，即代表还没有同步
        if (ObjectUtil.isEmpty(dto.getId())) {
            Long targetId = dto.getTargetId();
            String targetType = dto.getTargetType();

            String targetFavoriteKey = String.format(RedisKeyConstant.FAVORITE_ITEM, targetId, targetType, userId);

            favoriteItemsVo.setCannelFavorite(false);
            favoriteItemsVo.setLuu(userId);
            favoriteItemsVo.setLud(currentDateTime);
            redisUtils.hPut(RedisKeyConstant.SYNC_SAVE_FAVORITE,targetFavoriteKey,favoriteItemsVo);
        }else {
            favoriteItemsVo.setFavoriteId(dto.getTargetFavoriteId());
            favoriteItemsVo.setLuu(userId);
            favoriteItemsVo.setLud(currentDateTime);
            favoriteItemsDao.updateById(favoriteItemsVo);
        }
        return CourseResponseWrapper.getSuccess("移动成功");
    }
}