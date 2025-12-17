package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.dto.LikesDto;
import com.lyw.cloudInteraction.mapper.LikesDao;
import com.lyw.cloudInteraction.service.HeatEventPublisher;
import com.lyw.cloudInteraction.service.LikesBo;
import com.lyw.cloudInteraction.service.UserBehaviorPublisher;
import com.lyw.cloudInteraction.vo.LikesVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.reidsCache.setCache.DistributedSetCacheHelper;
import com.lyw.commonUtil.util.reidsCache.setCache.SetCacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * <p>
 * 点赞表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class LikesImpl extends ServiceImpl<LikesDao, LikesVo> implements LikesBo {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private DistributedSetCacheHelper distributedSetCacheHelper;
    @Resource
    private HeatEventPublisher heatEventPublisher;
    @Resource
    private UserBehaviorPublisher userBehaviorPublisher;
    @Override
    public CourseResponseWrapper addLike(LikesDto dto) {
        Long targetId = dto.getTargetId();
        String targetType = dto.getTargetType();
        Long userId = dto.getUserId();
        Long courseId = dto.getCourseId();
        String targetLikeKey = String.format(RedisKeyConstant.LIKE_ITEM, targetId, targetType);
        redisUtils.hPut(RedisKeyConstant.SYNC_SAVE_LIKE,targetLikeKey+":"+userId,1);

        // 使用 Set 记录用户点赞关系
        Long result = redisUtils.sAdd(targetLikeKey, userId);

        if (ObjectUtil.isNotEmpty(courseId)) {
            heatEventPublisher.publishEvent(targetType, courseId, CommonKeyConstant.LIKE,null);
            userBehaviorPublisher.sendUserBehavior(Long.valueOf(userId), courseId, CommonKeyConstant.LIKE);
        }

        if (result > 0) {
            // 更新排行榜：增加点赞数 TODO 异步给排行榜
            String rankKey = String.format(RedisKeyConstant.LIKE_COUNT, targetType);
            redisUtils.zIncrementScore(rankKey,  targetId.toString(),1);
        }
        return CourseResponseWrapper.getSuccess(result > 0 ? "点赞成功" : "已点赞");
    }

    @Override
    public CourseResponseWrapper removeLike(LikesDto dto) {
        Long targetId = dto.getTargetId();
        String targetType = dto.getTargetType();
        Long userId = dto.getUserId();
        Long courseId = dto.getCourseId();
        String targetLikeKey = String.format(RedisKeyConstant.LIKE_ITEM, targetId, targetType);
        redisUtils.hPut(RedisKeyConstant.SYNC_SAVE_LIKE,targetLikeKey+":"+userId,0);

        // 使用 Set 移除用户收藏关系
        Long result = redisUtils.sRemove(targetLikeKey, userId);
        userBehaviorPublisher.sendUserBehavior(Long.valueOf(userId), courseId, CommonKeyConstant.UNLIKE);
        if (result > 0) {
            // 更新排行榜：减少点赞数 TODO 异步给排行榜
            String rankKey = String.format(RedisKeyConstant.LIKE_COUNT, targetType);
            redisUtils.zIncrementScore(rankKey,targetId.toString(),-1);
        }
        return CourseResponseWrapper.getSuccess(result > 0 ? "取消点赞成功" : "未点赞");
    }

    @Override
    public CourseResponseWrapper getLikeStatus(String targetType, Long targetId, Long userId) {
        String targetFavoriteKey = String.format(RedisKeyConstant.LIKE_ITEM, targetId, targetType);
        String lockTargetFavoriteKey = String.format(RedisKeyConstant.LOCK_LIKE_ITEM, targetId, targetType);
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
                    put("isLiked", targetCache.contains(userId));
                    put("likedCount", targetCache.size());
                }}
        );
        return CourseResponseWrapper.getSuccess(result);
    }

    @Override
    public CourseResponseWrapper getLikesByUser() {
        // 并行执行数据库查询和Redis查询
        Long userId = Long.valueOf(CurUserUtil.getUserId());
        CompletableFuture<List<LikesVo>> dbFuture = CompletableFuture.supplyAsync(() -> baseMapper.selectList(new LambdaQueryWrapper<LikesVo>().eq(LikesVo::getUserId, userId)));

        CompletableFuture<Map<String, String>> redisFuture = CompletableFuture.supplyAsync(() -> redisUtils.hGetAll(RedisKeyConstant.SYNC_SAVE_LIKE));
        try {
            // 等待两个任务都完成，然后合并结果
            return dbFuture.thenCombine(redisFuture, (dbFavorites, pendingData) ->
                            mergeLikeData(userId, dbFavorites, pendingData))  // 传递userId
                    .thenApply(CourseResponseWrapper::getSuccess)
                    .get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return CourseResponseWrapper.getFailed("系統繁忙,請稍後再試！");
        }
    }
    /**
     * 合并数据库和Redis的点赞数据
     */
    private List<LikesVo> mergeLikeData(Long userId, List<LikesVo> dbLikes, Map<String, String> pendingData) {
        if (CollectionUtils.isEmpty(pendingData)) {
            return dbLikes;
        }
        List<LikesVo> addLike = new ArrayList<>();
        if (CollectionUtils.isEmpty(dbLikes)) {
            pendingData.entrySet().stream().forEach(item->{
                String[] split = item.getKey().split(":");
                if (StrUtil.equals(userId.toString(),split[3])) {
                    LikesVo likesVo = new LikesVo();
                    likesVo.setTargetId(Long.valueOf(split[1]));
                    likesVo.setTargetType(split[2]);
                    likesVo.setUserId(userId);
                    addLike.add(likesVo);
                }
            });
            return addLike;
        }

        dbLikes = dbLikes.stream().filter(item->{
            Long targetId = item.getTargetId();
            String targetType = item.getTargetType();
            String targetLikeKey = String.format(RedisKeyConstant.LIKE_ITEM, targetId, targetType)+":"+userId;
            String value = pendingData.get(targetLikeKey);
            if (!StrUtil.equals(value,"0")){
                return true;
            }
            LikesVo likesVo = new LikesVo();
            likesVo.setTargetId(targetId);
            likesVo.setTargetType(targetType);
            likesVo.setUserId(userId);
            addLike.add(likesVo);
            return false;
        }).collect(Collectors.toList());
        dbLikes.addAll(addLike);
        return dbLikes;
    }

}