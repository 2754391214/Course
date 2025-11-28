package com.lyw.cloudInteraction.service;

import com.lyw.cloudInteraction.dto.UserBehaviorMessage;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * 用户行为消息生产者服务
 */
@Slf4j
@Service
public class UserBehaviorProducerService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // 行为权重常量
    private static final BigDecimal WEIGHT_LIKE_REVIEW = new BigDecimal("0.3");
    private static final BigDecimal WEIGHT_UNLIKE_REVIEW = new BigDecimal("-0.3");
    private static final BigDecimal WEIGHT_USEFUL_REVIEW = new BigDecimal("0.4");
    private static final BigDecimal WEIGHT_UNUSEFUL_REVIEW = new BigDecimal("-0.4");
    private static final BigDecimal WEIGHT_LIKE_REPLY = new BigDecimal("0.2");
    private static final BigDecimal WEIGHT_UNLIKE_REPLY = new BigDecimal("-0.2");

    /**
     * 发送点赞评价行为消息
     */
    public void sendLikeReviewBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "LIKE_REVIEW", WEIGHT_LIKE_REVIEW);
        addContext(message, "targetType", "review", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送点赞评价行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 发送取消点赞评价行为消息
     */
    public void sendUnlikeReviewBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "UNLIKE_REVIEW", WEIGHT_UNLIKE_REVIEW);
        addContext(message, "targetType", "review", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送取消点赞评价行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 发送标记评价有用行为消息
     */
    public void sendUsefulReviewBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "USEFUL_REVIEW", WEIGHT_USEFUL_REVIEW);
        addContext(message, "targetType", "review", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送标记评价有用行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 发送取消标记评价有用行为消息
     */
    public void sendUnusefulReviewBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "UNUSEFUL_REVIEW", WEIGHT_UNUSEFUL_REVIEW);
        addContext(message, "targetType", "review", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送取消标记评价有用行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 发送点赞回复行为消息
     */
    public void sendLikeReplyBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "LIKE_REPLY", WEIGHT_LIKE_REPLY);
        addContext(message, "targetType", "reply", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送点赞回复行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 发送取消点赞回复行为消息
     */
    public void sendUnlikeReplyBehavior(Long userId, Long courseId, Long targetId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "UNLIKE_REPLY", WEIGHT_UNLIKE_REPLY);
        addContext(message, "targetType", "reply", "targetId", targetId.toString());
        sendMessage(message);
        log.debug("发送取消点赞回复行为消息成功: userId={}, courseId={}, targetId={}", userId, courseId, targetId);
    }

    /**
     * 创建用户行为消息
     */
    private UserBehaviorMessage createUserBehaviorMessage(Long userId, Long courseId, String behaviorType, BigDecimal weight) {
        UserBehaviorMessage userBehaviorMessage = new UserBehaviorMessage();
        userBehaviorMessage.setMessageId(UUID.randomUUID().toString());
        userBehaviorMessage.setUserId(userId);
        userBehaviorMessage.setCourseId(courseId);
        userBehaviorMessage.setBehaviorType(behaviorType);
        userBehaviorMessage.setBehaviorWeight(weight);
        userBehaviorMessage.setBehaviorTime(new Date());
        userBehaviorMessage.setCreateTime(new Date());
        return userBehaviorMessage;
    }

    /**
     * 添加上下文信息
     */
    private void addContext(UserBehaviorMessage message, String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("键值对数量必须为偶数");
        }

        java.util.Map<String, Object> context = new java.util.HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            context.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        message.setContext(context);
    }

    /**
     * 发送消息到 RabbitMQ
     */
    private void sendMessage(UserBehaviorMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitmqKeyConstant.USER_BEHAVIOR_EXCHANGE,
                    RabbitmqKeyConstant.USER_BEHAVIOR_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            log.error("发送用户行为消息失败: userId={}, courseId={}, behaviorType={}",
                    message.getUserId(), message.getCourseId(), message.getBehaviorType(), e);
            // 降级处理：记录到错误日志
            handleSendFailure(message, e);
        }
    }

    /**
     * 消息发送失败处理
     */
    private void handleSendFailure(UserBehaviorMessage message, Exception e) {
        log.warn("用户行为消息发送失败，记录到本地日志: messageId={}, userId={}, behaviorType={}",
                message.getMessageId(), message.getUserId(), message.getBehaviorType());
        // 可以在这里实现本地存储逻辑
    }
}