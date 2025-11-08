package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.config.RabbitMQConfig;
import com.lyw.cloudChoose.dto.UserBehaviorMessage;
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
    private static final BigDecimal WEIGHT_ENROLL = new BigDecimal("0.8");
    private static final BigDecimal WEIGHT_DROP = new BigDecimal("-0.5");
    private static final BigDecimal WEIGHT_WAITLIST_ENROLL = new BigDecimal("0.6");

    /**
     * 发送选课行为消息
     */
    public void sendEnrollmentBehavior(Long userId, Long courseId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "ENROLL", WEIGHT_ENROLL);
        sendMessage(message);
        log.info("发送选课行为消息成功: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 发送退课行为消息
     */
    public void sendDropBehavior(Long userId, Long courseId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "DROP", WEIGHT_DROP);
        sendMessage(message);
        log.info("发送退课行为消息成功: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 发送等待列表选课行为消息
     */
    public void sendWaitlistEnrollmentBehavior(Long userId, Long courseId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "WAITLIST_ENROLL", WEIGHT_WAITLIST_ENROLL);
        sendMessage(message);
        log.info("发送等待列表选课行为消息成功: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 发送课程查看行为消息
     */
    public void sendViewBehavior(Long userId, Long courseId) {
        UserBehaviorMessage message = createUserBehaviorMessage(userId, courseId, "VIEW", new BigDecimal("0.1"));
        sendMessage(message);
        log.debug("发送课程查看行为消息: userId={}, courseId={}", userId, courseId);
    }

    /**
     * 创建用户行为消息
     */
    private UserBehaviorMessage createUserBehaviorMessage(Long userId, Long courseId, String behaviorType, BigDecimal weight) {
        return new UserBehaviorMessage()
                .setMessageId(UUID.randomUUID().toString())
                .setUserId(userId)
                .setCourseId(courseId)
                .setBehaviorType(behaviorType)
                .setBehaviorWeight(weight)
                .setBehaviorTime(new Date())
                .setCreateTime(new Date());
    }

    /**
     * 发送消息到 RabbitMQ
     */
    private void sendMessage(UserBehaviorMessage message) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_BEHAVIOR_EXCHANGE,
                    RabbitMQConfig.USER_BEHAVIOR_ROUTING_KEY,
                    message
            );
        } catch (Exception e) {
            log.error("发送用户行为消息失败: userId={}, courseId={}, behaviorType={}",
                    message.getUserId(), message.getCourseId(), message.getBehaviorType(), e);
            // 这里可以添加降级处理，比如写入本地日志或数据库
            handleSendFailure(message, e);
        }
    }

    /**
     * 消息发送失败处理
     */
    private void handleSendFailure(UserBehaviorMessage message, Exception e) {
        // 降级方案：记录到错误日志或本地文件
        log.warn("用户行为消息发送失败，记录到本地日志: messageId={}, userId={}, behaviorType={}",
                message.getMessageId(), message.getUserId(), message.getBehaviorType());

        // 可以在这里实现本地存储逻辑
        // localStorageService.saveFailedMessage(message);
    }
}