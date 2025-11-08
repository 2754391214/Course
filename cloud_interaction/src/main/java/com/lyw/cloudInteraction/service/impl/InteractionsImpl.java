package com.lyw.cloudInteraction.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudInteraction.constant.RedisConstant;
import com.lyw.cloudInteraction.mapper.ReviewRepliesDao;
import com.lyw.cloudInteraction.mapper.ReviewsDao;
import com.lyw.cloudInteraction.service.UserBehaviorProducerService;
import com.lyw.cloudInteraction.utils.RedisOperationHelper;
import com.lyw.cloudInteraction.utils.RedisUtil;
import com.lyw.cloudInteraction.vo.InteractionsVo;
import com.lyw.cloudInteraction.vo.ReviewsVo;
import com.lyw.cloudInteraction.vo.ReviewRepliesVo;
import com.lyw.cloudInteraction.dto.InteractionsDto;
import com.lyw.cloudInteraction.mapper.InteractionsDao;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lyw.cloudInteraction.service.InteractionsBo;
import com.lyw.cloudInteraction.service.HeatEventPublisher;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 互动表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Slf4j
@Service
public class InteractionsImpl extends ServiceImpl<InteractionsDao, InteractionsVo> implements InteractionsBo {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisOperationHelper redisOperationHelper;

    @Resource
    private HeatEventPublisher heatEventPublisher;

    @Resource
    private ReviewsDao reviewsDao;

    @Resource
    private ReviewRepliesDao reviewRepliesDao;

    @Resource
    private UserBehaviorProducerService userBehaviorProducerService;

    @Override
    public CourseResponseWrapper likeReview(InteractionsDto dto) {
        // 检查是否已经点赞
        if (checkInteractionExists("review", dto.getTargetId(), dto.getUserId(), "like")) {
            return CourseResponseWrapper.getFailed("您已经点赞过该评价");
        }

        boolean redisSuccess = false;
        // 优先尝试Redis
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = createInteractionInRedis(dto, "review", "like")
                    && updateCountInRedis(RedisConstant.REVIEW_USEFUL_COUNT_KEY, dto.getTargetId(), 1);
        }

        // Redis操作失败或不可用，降级到数据库
        if (!redisSuccess) {
            boolean dbSuccess = createInteractionInDB(dto, "review", "like");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReview(dto, "LIKE");

        //发送用户行为消息到推荐模块
        sendUserBehaviorForReview(dto, "LIKE_REVIEW");

        return CourseResponseWrapper.getSuccess("点赞成功");
    }

    @Override
    public CourseResponseWrapper unlikeReview(InteractionsDto dto) {
        // 检查是否已经点赞
        if (!checkInteractionExists("review", dto.getTargetId(), dto.getUserId(), "like")) {
            return CourseResponseWrapper.getFailed("您尚未点赞该评价");
        }

        boolean redisSuccess = false;
        // 优先尝试Redis
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = deleteInteractionFromRedis(dto, "review", "like")
                    && updateCountInRedis(RedisConstant.REVIEW_USEFUL_COUNT_KEY, dto.getTargetId(), -1);
        }

        // Redis操作失败或不可用，降级到数据库
        if (!redisSuccess) {
            boolean dbSuccess = deleteInteractionFromDB(dto, "review", "like");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReview(dto, "UNLIKE");

        //发送用户行为消息到推荐模块
        sendUserBehaviorForReview(dto, "UNLIKE_REVIEW");
        return CourseResponseWrapper.getSuccess("取消点赞成功");
    }

    @Override
    public CourseResponseWrapper markReviewUseful(InteractionsDto dto) {
        if (checkInteractionExists("review", dto.getTargetId(), dto.getUserId(), "useful")) {
            return CourseResponseWrapper.getFailed("您已经标记过该评价");
        }

        boolean redisSuccess = false;
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = createInteractionInRedis(dto, "review", "useful")
                    && updateCountInRedis(RedisConstant.REVIEW_LIKE_COUNT_KEY, dto.getTargetId(), 1);
        }

        if (!redisSuccess) {
            boolean dbSuccess = createInteractionInDB(dto, "review", "useful");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReview(dto, "USEFUL");

        // 发送用户行为消息到推荐模块
        sendUserBehaviorForReview(dto, "USEFUL_REVIEW");

        return CourseResponseWrapper.getSuccess("标记有用成功");
    }

    @Override
    public CourseResponseWrapper unmarkReviewUseful(InteractionsDto dto) {
        if (!checkInteractionExists("review", dto.getTargetId(), dto.getUserId(), "useful")) {
            return CourseResponseWrapper.getFailed("您尚未标记该评价为有用");
        }

        boolean redisSuccess = false;
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = deleteInteractionFromRedis(dto, "review", "useful")
                    && updateCountInRedis(RedisConstant.REVIEW_LIKE_COUNT_KEY, dto.getTargetId(), -1);
        }

        if (!redisSuccess) {
            boolean dbSuccess = deleteInteractionFromDB(dto, "review", "useful");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReview(dto, "UNUSEFUL");

        // 发送用户行为消息到推荐模块
        sendUserBehaviorForReview(dto, "UNUSEFUL_REVIEW");

        return CourseResponseWrapper.getSuccess("取消有用标记成功");
    }

    @Override
    public CourseResponseWrapper likeReply(InteractionsDto dto) {
        if (checkInteractionExists("reply", dto.getTargetId(), dto.getUserId(), "like")) {
            return CourseResponseWrapper.getFailed("您已经点赞过该回复");
        }

        boolean redisSuccess = false;
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = createInteractionInRedis(dto, "reply", "like")
                    && updateCountInRedis(RedisConstant.REPLY_LIKE_COUNT_KEY, dto.getTargetId(), 1);
        }

        if (!redisSuccess) {
            boolean dbSuccess = createInteractionInDB(dto, "reply", "like");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReply(dto, "LIKE");

        // 发送用户行为消息到推荐模块
        sendUserBehaviorForReply(dto, "LIKE_REPLY");

        return CourseResponseWrapper.getSuccess("点赞回复成功");
    }

    @Override
    public CourseResponseWrapper unlikeReply(InteractionsDto dto) {
        if (!checkInteractionExists("reply", dto.getTargetId(), dto.getUserId(), "like")) {
            return CourseResponseWrapper.getFailed("您尚未点赞该回复");
        }

        boolean redisSuccess = false;
        if (redisOperationHelper.isRedisAvailable()) {
            redisSuccess = deleteInteractionFromRedis(dto, "reply", "like")
                    && updateCountInRedis(RedisConstant.REPLY_LIKE_COUNT_KEY, dto.getTargetId(), -1);
        }

        if (!redisSuccess) {
            boolean dbSuccess = deleteInteractionFromDB(dto, "reply", "like");
            if (!dbSuccess) {
                return CourseResponseWrapper.getFailed("操作失败，请稍后重试");
            }
        }

        // 发送热度事件到排行榜模块
        sendHeatEventForReply(dto, "UNLIKE");

        // 发送用户行为消息到推荐模块
        sendUserBehaviorForReply(dto, "UNLIKE_REPLY");

        return CourseResponseWrapper.getSuccess("取消点赞回复成功");
    }

    @Override
    public CourseResponseWrapper getInteractionStatus(String targetType, Long targetId, Long userId, String interactionType) {
        boolean exists = checkInteractionExists(targetType, targetId, userId, interactionType);
        return CourseResponseWrapper.getSuccess(exists);
    }

    @Override
    public CourseResponseWrapper getUserInteractions(Long userId, String targetType, String interactionType) {
        // 直接查询数据库，确保数据完整性
        LambdaQueryWrapper<InteractionsVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InteractionsVo::getUserId, userId)
                .eq(StrUtil.isNotBlank(targetType), InteractionsVo::getTargetType, targetType)
                .eq(StrUtil.isNotBlank(interactionType), InteractionsVo::getInteractionType, interactionType)
                .orderByDesc(InteractionsVo::getCrd);
        List<InteractionsVo> interactions = getBaseMapper().selectList(queryWrapper);
        return CourseResponseWrapper.getSuccess(interactions);
    }

    /**
     * 为评价发送用户行为消息
     */
    private void sendUserBehaviorForReview(InteractionsDto dto, String behaviorType) {
        try {
            // 获取评价对应的课程ID
            ReviewsVo review = reviewsDao.selectById(dto.getTargetId());
            if (review == null) {
                log.warn("未找到评价信息: targetId={}", dto.getTargetId());
                return;
            }

            Long courseId = review.getCourseId();
            if (courseId == null) {
                log.warn("评价没有关联课程: targetId={}", dto.getTargetId());
                return;
            }

            // 根据行为类型发送消息
            switch (behaviorType) {
                case "LIKE_REVIEW":
                    userBehaviorProducerService.sendLikeReviewBehavior(dto.getUserId(), courseId, dto.getTargetId());
                    break;
                case "UNLIKE_REVIEW":
                    userBehaviorProducerService.sendUnlikeReviewBehavior(dto.getUserId(), courseId, dto.getTargetId());
                    break;
                case "USEFUL_REVIEW":
                    userBehaviorProducerService.sendUsefulReviewBehavior(dto.getUserId(), courseId, dto.getTargetId());
                    break;
                case "UNUSEFUL_REVIEW":
                    userBehaviorProducerService.sendUnusefulReviewBehavior(dto.getUserId(), courseId, dto.getTargetId());
                    break;
            }

        } catch (Exception e) {
            log.error("发送评价用户行为消息失败: behaviorType={}, targetId={}", behaviorType, dto.getTargetId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 为回复发送用户行为消息
     */
    private void sendUserBehaviorForReply(InteractionsDto dto, String behaviorType) {
        try {
            // 获取回复对应的评价，再获取课程ID
            ReviewRepliesVo reply = reviewRepliesDao.selectById(dto.getTargetId());
            if (reply == null) {
                log.warn("未找到回复信息: targetId={}", dto.getTargetId());
                return;
            }

            // 通过回复找到评价
            ReviewsVo review = reviewsDao.selectById(reply.getReviewId());
            if (review == null) {
                log.warn("未找到回复对应的评价: replyId={}, reviewId={}", dto.getTargetId(), reply.getReviewId());
                return;
            }

            Long courseId = review.getCourseId();
            if (courseId == null) {
                log.warn("评价没有关联课程: reviewId={}", reply.getReviewId());
                return;
            }

            // 根据行为类型发送消息
            if ("LIKE_REPLY".equals(behaviorType)) {
                userBehaviorProducerService.sendLikeReplyBehavior(dto.getUserId(), courseId, dto.getTargetId());
            } else if ("UNLIKE_REPLY".equals(behaviorType)) {
                userBehaviorProducerService.sendUnlikeReplyBehavior(dto.getUserId(), courseId, dto.getTargetId());
            }

        } catch (Exception e) {
            log.error("发送回复用户行为消息失败: behaviorType={}, targetId={}", behaviorType, dto.getTargetId(), e);
            // 不抛出异常，避免影响主流程
        }
    }
    /**
     * 为评价发送热度事件
     */
    private void sendHeatEventForReview(InteractionsDto dto, String eventType) {
        try {
            // 获取评价对应的课程ID
            ReviewsVo review = reviewsDao.selectById(dto.getTargetId());
            if (review == null) {
                log.warn("未找到评价信息: targetId={}", dto.getTargetId());
                return;
            }

            Long courseId = review.getCourseId();
            if (courseId == null) {
                log.warn("评价没有关联课程: targetId={}", dto.getTargetId());
                return;
            }

            // 根据事件类型发送消息
            switch (eventType) {
                case "LIKE":
                    heatEventPublisher.publishLikeEvent("review", dto.getTargetId(), dto.getUserId(), courseId);
                    break;
                case "UNLIKE":
                    heatEventPublisher.publishUnlikeEvent("review", dto.getTargetId(), dto.getUserId(), courseId);
                    break;
                case "USEFUL":
                    heatEventPublisher.publishUsefulEvent("review", dto.getTargetId(), dto.getUserId(), courseId);
                    break;
                case "UNUSEFUL":
                    heatEventPublisher.publishUnusefulEvent("review", dto.getTargetId(), dto.getUserId(), courseId);
                    break;
            }

        } catch (Exception e) {
            log.error("发送评价热度事件失败: eventType={}, targetId={}", eventType, dto.getTargetId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 为回复发送热度事件
     */
    private void sendHeatEventForReply(InteractionsDto dto, String eventType) {
        try {
            // 获取回复对应的评价，再获取课程ID
            ReviewRepliesVo reply = reviewRepliesDao.selectById(dto.getTargetId());
            if (reply == null) {
                log.warn("未找到回复信息: targetId={}", dto.getTargetId());
                return;
            }

            // 通过回复找到评价
            ReviewsVo review = reviewsDao.selectById(reply.getReviewId());
            if (review == null) {
                log.warn("未找到回复对应的评价: replyId={}, reviewId={}", dto.getTargetId(), reply.getReviewId());
                return;
            }

            Long courseId = review.getCourseId();
            if (courseId == null) {
                log.warn("评价没有关联课程: reviewId={}", reply.getReviewId());
                return;
            }

            // 根据事件类型发送消息
            if ("LIKE".equals(eventType)) {
                heatEventPublisher.publishLikeEvent("reply", dto.getTargetId(), dto.getUserId(), courseId);
            } else if ("UNLIKE".equals(eventType)) {
                heatEventPublisher.publishUnlikeEvent("reply", dto.getTargetId(), dto.getUserId(), courseId);
            }

        } catch (Exception e) {
            log.error("发送回复热度事件失败: eventType={}, targetId={}", eventType, dto.getTargetId(), e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 检查互动状态（Redis + MySQL双重检查）
     */
    private boolean checkInteractionExists(String targetType, Long targetId, Long userId, String interactionType) {
        // 先检查Redis
        if (redisOperationHelper.isRedisAvailable()) {
            try {
                String field = RedisConstant.generateInteractionField(targetType, targetId, userId, interactionType);
                boolean existsInRedis = redisOperationHelper.executeWithRetry(() ->
                        redisUtil.hExists(RedisConstant.INTERACTION_HASH_KEY, field));

                if (existsInRedis) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("Redis检查失败，降级到数据库检查", e);
            }
        }

        // Redis不可用或检查失败，降级到数据库检查
        return checkInteractionExistsInDB(targetType, targetId, userId, interactionType);
    }

    /**
     * 数据库检查互动状态
     */
    private boolean checkInteractionExistsInDB(String targetType, Long targetId, Long userId, String interactionType) {
        LambdaQueryWrapper<InteractionsVo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InteractionsVo::getUserId, userId)
                .eq(InteractionsVo::getTargetType, targetType)
                .eq(InteractionsVo::getTargetId, targetId)
                .eq(InteractionsVo::getInteractionType, interactionType);
        return getBaseMapper().selectCount(queryWrapper) > 0;
    }

    /**
     * 创建互动记录到数据库（保底操作）
     */
    private boolean createInteractionInDB(InteractionsDto dto, String targetType, String interactionType) {
        try {
            // 检查是否已存在
            if (checkInteractionExistsInDB(targetType, dto.getTargetId(), dto.getUserId(), interactionType)) {
                log.info("互动记录已存在，跳过创建: {}/{}/{}", targetType, dto.getTargetId(), interactionType);
                return false;
            }

            InteractionsVo interaction = new InteractionsVo();
            interaction.setUserId(dto.getUserId())
                    .setTargetType(targetType)
                    .setTargetId(dto.getTargetId())
                    .setInteractionType(interactionType)
                    .setCru(CurUserUtil.getUserCode())
                    .setLuu(CurUserUtil.getUserCode())
                    .setCrd(DateTimeUtils.getCurrentDateTime())
                    .setLud(DateTimeUtils.getCurrentDateTime());

            return getBaseMapper().insert(interaction) > 0;
        } catch (Exception e) {
            log.error("创建互动记录到数据库失败", e);
            return false;
        }
    }

    /**
     * 删除互动记录从数据库（保底操作）
     */
    private boolean deleteInteractionFromDB(InteractionsDto dto, String targetType, String interactionType) {
        try {
            LambdaQueryWrapper<InteractionsVo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InteractionsVo::getUserId, dto.getUserId())
                    .eq(InteractionsVo::getTargetType, targetType)
                    .eq(InteractionsVo::getTargetId, dto.getTargetId())
                    .eq(InteractionsVo::getInteractionType, interactionType);

            return getBaseMapper().delete(queryWrapper) > 0;
        } catch (Exception e) {
            log.error("从数据库删除互动记录失败", e);
            return false;
        }
    }

    /**
     * 创建Redis互动记录
     */
    private boolean createInteractionInRedis(InteractionsDto dto, String targetType, String interactionType) {
        try {
            String field = RedisConstant.generateInteractionField(targetType, dto.getTargetId(), dto.getUserId(), interactionType);

            Map<String, Object> interactionData = new HashMap<>();
            interactionData.put("userId", dto.getUserId());
            interactionData.put("targetType", targetType);
            interactionData.put("targetId", dto.getTargetId());
            interactionData.put("interactionType", interactionType);
            interactionData.put("cru", CurUserUtil.getUserCode());
            interactionData.put("luu", CurUserUtil.getUserCode());
            interactionData.put("crd", DateTimeUtils.getCurrentDateTime());
            interactionData.put("lud", DateTimeUtils.getCurrentDateTime());
            interactionData.put("syncStatus", 0);

            redisOperationHelper.executeWithRetry(() -> {
                redisUtil.hSet(RedisConstant.INTERACTION_HASH_KEY, field, interactionData);
                return null;
            });

            return true;
        } catch (Exception e) {
            log.error("创建Redis互动记录失败", e);
            return false;
        }
    }

    /**
     * 删除Redis互动记录
     */
    private boolean deleteInteractionFromRedis(InteractionsDto dto, String targetType, String interactionType) {
        try {
            String field = RedisConstant.generateInteractionField(targetType, dto.getTargetId(), dto.getUserId(), interactionType);
            redisOperationHelper.executeWithRetry(() -> {
                redisUtil.hDelete(RedisConstant.INTERACTION_HASH_KEY, field);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("删除Redis互动记录失败", e);
            return false;
        }
    }

    /**
     * 更新计数
     */
    private boolean updateCountInRedis(String countKey, Long targetId, long delta) {
        try {
            redisOperationHelper.executeWithRetry(() -> {
                redisUtil.hIncrement(countKey, targetId.toString(), delta);
                return null;
            });
            return true;
        } catch (Exception e) {
            log.error("更新Redis计数失败: {}/{}", countKey, targetId, e);
            return false;
        }
    }
}