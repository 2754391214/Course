package com.lyw.cloudInteraction.service;

import com.lyw.cloudInteraction.dto.HeatEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class HeatEventPublisher {

    // 使用排行榜模块定义的交换机和路由键
    private static final String COURSE_HEAT_EXCHANGE = "course.heat.exchange";
    private static final String COURSE_HEAT_ROUTING_KEY_PREFIX = "course.heat.";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送点赞事件
     */
    public void publishLikeEvent(String targetType, Long targetId, Long userId, Long courseId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("LIKE")  // 与消费端的事件类型匹配
                .setCourseId(courseId)
                .setUserId(userId)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setAction("ADD")  // 明确操作类型
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送点赞事件: targetType={}, targetId={}, courseId={}", targetType, targetId, courseId);
    }

    /**
     * 发送取消点赞事件
     */
    public void publishUnlikeEvent(String targetType, Long targetId, Long userId, Long courseId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("LIKE")  // 事件类型仍然是LIKE，但action是REMOVE
                .setCourseId(courseId)
                .setUserId(userId)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setAction("REMOVE")  // 取消操作
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送取消点赞事件: targetType={}, targetId={}, courseId={}", targetType, targetId, courseId);
    }

    /**
     * 发送标记有用事件
     */
    public void publishUsefulEvent(String targetType, Long targetId, Long userId, Long courseId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("FAVORITE")  // 使用FAVORITE事件类型
                .setCourseId(courseId)
                .setUserId(userId)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setAction("ADD")
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送标记有用事件: targetType={}, targetId={}, courseId={}", targetType, targetId, courseId);
    }

    /**
     * 发送取消标记有用事件
     */
    public void publishUnusefulEvent(String targetType, Long targetId, Long userId, Long courseId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("FAVORITE")  // 使用FAVORITE事件类型
                .setCourseId(courseId)
                .setUserId(userId)
                .setTargetType(targetType)
                .setTargetId(targetId)
                .setAction("REMOVE")
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送取消标记有用事件: targetType={}, targetId={}, courseId={}", targetType, targetId, courseId);
    }

    /**
     * 发送热度事件到消息队列
     */
    private void sendHeatEvent(HeatEventMessage message) {
        try {
            String routingKey = COURSE_HEAT_ROUTING_KEY_PREFIX + message.getEventType().toLowerCase();
            rabbitTemplate.convertAndSend(COURSE_HEAT_EXCHANGE, routingKey, message);

            log.debug("热度事件发送成功: eventId={}, eventType={}, courseId={}",
                    message.getEventId(), message.getEventType(), message.getCourseId());

        } catch (Exception e) {
            log.error("发送热度事件失败: eventId={}", message.getEventId(), e);
            // 这里可以选择记录到日志或数据库，但不影响主流程
        }
    }
}