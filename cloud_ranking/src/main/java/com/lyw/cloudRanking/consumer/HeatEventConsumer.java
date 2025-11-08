package com.lyw.cloudRanking.consumer;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyw.cloudRanking.dto.HeatEventMessage;
import com.lyw.cloudRanking.service.HeatCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HeatEventConsumer {

    @Resource
    private HeatCalculateService heatCalculateService;
    @Resource
    private ObjectMapper objectMapper;
    // 批量处理缓冲区
    private final Map<Long, List<HeatEventMessage>> buffer = new HashMap<>();
    private static final int BATCH_THRESHOLD = 50;
    private static final long BUFFER_TIMEOUT = 100; // 100ms

    /**
     * 单消息消费
     */
    @RabbitListener(queues = "course.heat.queue")
    public void handleSingleMessage(HeatEventMessage message) {
        log.debug("收到热度事件: eventType={}, courseId={}",
                message.getEventType(), message.getCourseId());
        try {
            // 使用缓冲处理，减少Redis操作
            bufferMessage(message);

        } catch (Exception e) {
            log.error("处理热度事件失败: eventId={}", message.getEventId(), e);
            // 快速失败，不重试，保证系统稳定性
        }
    }

    /**
     * 批量消息消费
     */
    @RabbitListener(queues = "course.heat.queue", containerFactory = "batchContainerFactory")
    public void handleBatchMessages(List<Message> messages) {
        log.info("批量处理热度事件: 数量={}", messages.size());

        Map<Long, Double> courseIncrements = new HashMap<>();

        for (Message message : messages) {
            try {
                HeatEventMessage event = convertToHeatEvent(message);
                double increment = heatCalculateService.calculateHeatIncrement(event);

                // 按课程聚合增量
                courseIncrements.merge(event.getCourseId(), increment, Double::sum);

            } catch (Exception e) {
                log.error("处理批量消息失败", e);
            }
        }

        // 批量更新热度
        heatCalculateService.batchUpdateHeat(courseIncrements);

        log.info("批量更新完成: 影响课程数={}", courseIncrements.size());
    }

    /**
     * 缓冲消息，减少Redis操作
     */
    private synchronized void bufferMessage(HeatEventMessage message) {
        Long courseId = message.getCourseId();
        List<HeatEventMessage> courseBuffer = buffer.computeIfAbsent(courseId, k -> new ArrayList<>());

        courseBuffer.add(message);

        // 达到阈值立即处理
        if (courseBuffer.size() >= BATCH_THRESHOLD) {
            processBufferedEvents(courseId);
        }

        // 设置超时处理（简化实现，实际应该用定时任务）
        if (courseBuffer.size() == 1) {
            new Thread(() -> {
                try {
                    Thread.sleep(BUFFER_TIMEOUT);
                    processBufferedEvents(courseId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    /**
     * 处理缓冲的事件
     */
    private void processBufferedEvents(Long courseId) {
        List<HeatEventMessage> eventsToProcess;

        synchronized (buffer) {
            eventsToProcess = buffer.remove(courseId);
            if (CollectionUtil.isEmpty(eventsToProcess)) {
                return;
            }
        }

        // 计算总增量
        double totalIncrement = eventsToProcess.stream()
                .mapToDouble(heatCalculateService::calculateHeatIncrement)
                .sum();

        // 更新热度
        heatCalculateService.updateCourseHeat(courseId, totalIncrement);

        log.debug("批量处理课程{}的{}个事件，总增量{}",
                courseId, eventsToProcess.size(), totalIncrement);
    }

    /**
     * 转换消息
     */
    private HeatEventMessage convertToHeatEvent(Message message) {
        try {
            byte[] body = message.getBody();

            if (body == null || body.length == 0) {
                log.warn("收到空消息体");
                return null;
            }

            // 使用Jackson反序列化消息体
            HeatEventMessage heatEvent = objectMapper.readValue(body, HeatEventMessage.class);

            // 验证必要字段
            if (heatEvent.getEventId() == null) {
                log.warn("消息缺少eventId: {}", new String(body));
                return null;
            }

            if (heatEvent.getCourseId() == null) {
                log.warn("消息缺少courseId: eventId={}", heatEvent.getEventId());
                return null;
            }

            if (heatEvent.getEventType() == null) {
                log.warn("消息缺少eventType: eventId={}", heatEvent.getEventId());
                return null;
            }

            log.debug("成功转换消息: eventId={}, eventType={}, courseId={}",
                    heatEvent.getEventId(), heatEvent.getEventType(), heatEvent.getCourseId());

            return heatEvent;

        } catch (Exception e) {
            log.error("消息转换失败: messageProperties={}", message.getMessageProperties(), e);
            throw new RuntimeException("消息转换失败", e);
        }
    }
}