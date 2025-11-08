package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.HeatEventMessage;
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
     * 发送选课事件
     */
    public void publishEnrollmentEvent(Long courseId, Long studentId, Long teacherId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ENROLLMENT")
                .setCourseId(courseId)
                .setUserId(studentId)
                .setTeacherId(teacherId)
                .setAction("ADD")
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送选课事件: courseId={}, studentId={}", courseId, studentId);
    }

    /**
     * 发送退课事件
     */
    public void publishWithdrawalEvent(Long courseId, Long studentId, Long teacherId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("WITHDRAWAL")
                .setCourseId(courseId)
                .setUserId(studentId)
                .setTeacherId(teacherId)
                .setAction("REMOVE")
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送退课事件: courseId={}, studentId={}", courseId, studentId);
    }

    /**
     * 发送等待列表选课事件
     */
    public void publishWaitlistEnrollmentEvent(Long courseId, Long studentId, Long teacherId) {
        HeatEventMessage message = new HeatEventMessage()
                .setEventId(UUID.randomUUID().toString())
                .setEventType("ENROLLMENT")
                .setCourseId(courseId)
                .setUserId(studentId)
                .setTeacherId(teacherId)
                .setAction("ADD")
                .setEventTime(new Date());

        sendHeatEvent(message);
        log.debug("发送等待列表选课事件: courseId={}, studentId={}", courseId, studentId);
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