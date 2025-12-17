package com.lyw.cloudInteraction.service;

import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import com.lyw.commonUtil.message.UserBehaviorMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.UUID;

/**
 * 用户行为消息生产者服务
 */
@Slf4j
@Service
public class UserBehaviorPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送用户行为消息
     */
    public void sendUserBehavior(Long userId, Long courseId, String type) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, type);
        sendMessage(message);
        log.debug("发送用户行为消息成功: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 创建用户行为消息
     */
    private UserBehaviorMessage createUserBehaviorMessage(Long userId, Long courseId, String behaviorType) {
        UserBehaviorMessage userBehaviorMessage = new UserBehaviorMessage();
        userBehaviorMessage.setMessageId(UUID.randomUUID().toString());
        userBehaviorMessage.setUserId(userId);
        userBehaviorMessage.setCourseId(courseId);
        userBehaviorMessage.setBehaviorType(behaviorType);
        userBehaviorMessage.setBehaviorTime(new Date());
        return userBehaviorMessage;
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