package com.lyw.cloudInteraction.service;

import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import com.lyw.commonUtil.message.HeatEventMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class HeatEventPublisher {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送热度事件
     */
    public void publishEvent(String targetType, Long courseId, String eventType, Double ratingValue) {
        HeatEventMessage message = new HeatEventMessage()
                .setMessageId(UUID.randomUUID().toString())
                .setEventType(eventType)  // 与消费端的事件类型匹配
                .setTargetType(targetType)
                .setCourseId(courseId)
                .setRatingValue(ratingValue)
                .setEventTime(new Date());
        sendHeatEvent(message);
        log.debug("发送热度事件: targetType={}, courseId={}, eventType={}", targetType, courseId, eventType);
    }

    /**
     * 发送热度事件到消息队列
     */
    private void sendHeatEvent(HeatEventMessage message) {
        try {
            String routingKey = RabbitmqKeyConstant.COURSE_HEAT_ROUTING_KEY_PREFIX + message.getEventType().toLowerCase();
            rabbitTemplate.convertAndSend(RabbitmqKeyConstant.COURSE_HEAT_EXCHANGE, routingKey, message);

            log.debug("热度事件发送成功: messageId={}, eventType={}, courseId={}",
                    message.getMessageId(), message.getEventType(), message.getCourseId());

        } catch (Exception e) {
            log.error("发送热度事件失败: messageId={}", message.getMessageId(), e);
            // 这里可以选择记录到日志或数据库，但不影响主流程
        }
    }
}