package com.lyw.cloudRanking.consumer;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyw.commonUtil.message.HeatEventMessage;
import com.lyw.cloudRanking.service.HeatCalculateService;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
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
    // 批量处理缓冲区
    private final Map<Long, List<HeatEventMessage>> buffer = new HashMap<>();
    private static final int BATCH_THRESHOLD = 50;
    private static final long BUFFER_TIMEOUT = 100; // 100ms

    /**
     * 单消息消费
     */
    @RabbitListener(queues = RabbitmqKeyConstant.COURSE_HEAT_QUEUE)
    public void handleSingleMessage(HeatEventMessage message) {
        log.debug("收到热度事件: eventType={}, courseId={}", message.getEventType(), message.getCourseId());
        try {
            // 使用缓冲处理，减少Redis操作
            bufferMessage(message);

        } catch (Exception e) {
            log.error("处理热度事件失败: messageId={}", message.getMessageId(), e);
            // 快速失败，不重试，保证系统稳定性
        }
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
}