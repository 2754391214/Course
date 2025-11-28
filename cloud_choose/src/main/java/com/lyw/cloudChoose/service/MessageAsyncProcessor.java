package com.lyw.cloudChoose.service;

import com.alibaba.fastjson.JSON;
import com.lyw.cloudChoose.vo.LocalMessageVo;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MessageAsyncProcessor {

    @Resource
    private ThreadPoolTaskExecutor messageExecutor;

    @Resource
    private LocalMessageService localMessageService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    private final BlockingQueue<LocalMessageVo> messageQueue = new LinkedBlockingQueue<>(1);

    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        // 启动批量消息处理器
        messageExecutor.execute(this::batchProcessMessages);
        log.info("MessageAsyncProcessor 初始化完成，开始处理消息");
    }

    @PreDestroy
    public void destroy() {
        running = false;
        log.info("MessageAsyncProcessor 停止运行");
    }

    /**
     * 提交单个消息到队列
     */
    public boolean submitMessage(LocalMessageVo message) {
        try {
            boolean success = messageQueue.offer(message, 100, TimeUnit.MILLISECONDS);
            if (!success) {
                log.warn("消息队列已满，直接同步发送: businessKey={}", message.getBusinessKey());
                return localMessageService.sendMessage(message);
            }
            log.debug("消息提交到队列成功: businessKey={}, 队列大小={}",
                    message.getBusinessKey(), messageQueue.size());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("提交消息被中断，直接同步发送: businessKey={}", message.getBusinessKey());
            return localMessageService.sendMessage(message);
        }
    }

    /**
     * 批量提交消息到队列
     */
    public boolean submitMessages(List<LocalMessageVo> messages) {
        if (messages == null || messages.isEmpty()) {
            return true;
        }

        int successCount = 0;
        for (LocalMessageVo message : messages) {
            if (submitMessage(message)) {
                successCount++;
            }
        }

        log.info("批量提交消息完成: 总数={}, 成功={}, 当前队列大小={}",
                messages.size(), successCount, messageQueue.size());
        return successCount == messages.size();
    }

    /**
     * 批量处理消息的主循环
     */
    private void batchProcessMessages() {
        List<LocalMessageVo> batch = new ArrayList<>(100);

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                batch.clear();

                // 阻塞获取第一条消息，最多等待500ms
                LocalMessageVo firstMessage = messageQueue.poll(500, TimeUnit.MILLISECONDS);
                if (firstMessage != null) {
                    batch.add(firstMessage);
                    // 非阻塞获取剩余消息，最多再取99条
                    messageQueue.drainTo(batch, 99);
                }

                if (!batch.isEmpty()) {
                    processMessageBatch(batch);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("MessageAsyncProcessor 处理线程被中断");
                break;
            } catch (Exception e) {
                log.error("批量处理消息异常", e);
                // 防止异常导致线程退出
                safeSleep(1000);
            }
        }

        // 处理剩余消息
        processRemainingMessages();
        log.info("MessageAsyncProcessor 处理线程退出");
    }

    /**
     * 处理消息批次
     */
    private void processMessageBatch(List<LocalMessageVo> batch) {
        long startTime = System.currentTimeMillis();
        log.debug("开始处理消息批次: 数量={}", batch.size());

        try {
            // 1. 批量发送到 RabbitMQ
            List<MessageSendResult> sendResults = batchSendToRabbitMQ(batch);

            // 2. 批量更新消息状态
            batchUpdateMessageStatus(sendResults);

            log.info("消息批次处理完成: 数量={}, 成功={}, 失败={}, 耗时={}ms",
                    batch.size(),
                    countSuccess(sendResults),
                    countFailures(sendResults),
                    System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("处理消息批次失败: 数量={}", batch.size(), e);
            // 降级为单条处理
            fallbackToSingleProcessing(batch);
        }
    }

    /**
     * 批量发送到 RabbitMQ
     */
    private List<MessageSendResult> batchSendToRabbitMQ(List<LocalMessageVo> messages) {
        List<MessageSendResult> results = new ArrayList<>(messages.size());

        for (LocalMessageVo message : messages) {
            MessageSendResult result = new MessageSendResult(message);
            try {
                Object body = JSON.parse(message.getMessageBody());
                rabbitTemplate.convertAndSend(
                        message.getExchange(),
                        message.getRoutingKey(),
                        body
                );
                result.setSuccess(true);
                result.setSentMessage(message);
            } catch (Exception e) {
                result.setSuccess(false);
                result.setError(e.getMessage());
                log.error("发送消息失败: businessKey={}", message.getBusinessKey(), e);
            }
            results.add(result);
        }

        return results;
    }

    /**
     * 批量更新消息状态
     */
    private void batchUpdateMessageStatus(List<MessageSendResult> sendResults) {
        List<LocalMessageVo> successMessages = new ArrayList<>();
        List<LocalMessageVo> failedMessages = new ArrayList<>();
        String currentTime = DateTimeUtils.getCurrentDateTime();

        for (MessageSendResult result : sendResults) {
            LocalMessageVo message = result.getMessage();

            if (result.isSuccess()) {
                // 发送成功
                message.setStatus(LocalMessageVo.STATUS_SENT);
                message.setLud(currentTime);
                successMessages.add(message);
            } else {
                // 发送失败，处理重试逻辑
                handleFailedMessage(message, result.getError(), currentTime);
                failedMessages.add(message);
            }
        }

        // 批量更新成功消息
        if (!successMessages.isEmpty()) {
            localMessageService.updateBatchByIds(successMessages);
        }

        // 批量更新失败消息
        if (!failedMessages.isEmpty()) {
            localMessageService.updateBatchByIds(failedMessages);
        }
    }

    /**
     * 处理失败消息
     */
    private void handleFailedMessage(LocalMessageVo message, String error, String currentTime) {
        int newRetryCount = message.getRetryCount() + 1;
        message.setRetryCount(newRetryCount);
        message.setErrorMessage(error);
        message.setLud(currentTime);

        if (newRetryCount >= message.getMaxRetryCount()) {
            message.setStatus(LocalMessageVo.STATUS_FINAL_FAILURE);
            log.error("消息达到最大重试次数: businessKey={}, retryCount={}",
                    message.getBusinessKey(), newRetryCount);
        } else {
            message.setStatus(LocalMessageVo.STATUS_FAILED);
            message.setNextRetryTime(calculateNextRetryTime(newRetryCount));

            // 重新加入队列等待重试
            if (running) {
                submitMessage(message);
            }
        }
    }

    /**
     * 降级为单条处理
     */
    private void fallbackToSingleProcessing(List<LocalMessageVo> batch) {
        log.warn("批量处理失败，降级为单条处理: 数量={}", batch.size());

        for (LocalMessageVo message : batch) {
            try {
                localMessageService.sendMessage(message);
            } catch (Exception e) {
                log.error("降级单条处理也失败: businessKey={}", message.getBusinessKey(), e);
            }
        }
    }

    /**
     * 处理剩余消息
     */
    private void processRemainingMessages() {
        if (messageQueue.isEmpty()) {
            return;
        }

        List<LocalMessageVo> remaining = new ArrayList<>();
        messageQueue.drainTo(remaining);

        if (!remaining.isEmpty()) {
            log.info("处理剩余消息: 数量={}", remaining.size());
            processMessageBatch(remaining);
        }
    }

    /**
     * 安全的睡眠
     */
    private void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 计算下次重试时间
     */
    private Date calculateNextRetryTime(int retryCount) {
        // 指数退避: 2^retryCount 秒，最大60秒
        long delaySeconds = Math.min(60, (long) Math.pow(2, retryCount));
        return new Date(System.currentTimeMillis() + delaySeconds * 1000);
    }

    /**
     * 统计成功数量
     */
    private long countSuccess(List<MessageSendResult> results) {
        return results.stream().filter(MessageSendResult::isSuccess).count();
    }

    /**
     * 统计失败数量
     */
    private long countFailures(List<MessageSendResult> results) {
        return results.stream().filter(result -> !result.isSuccess()).count();
    }

    /**
     * 获取队列状态（监控用）
     */
    public Map<String, Object> getQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", messageQueue.size());
        status.put("remainingCapacity", messageQueue.remainingCapacity());
        status.put("running", running);
        return status;
    }

    /**
     * 消息发送结果
     */
    @Data
    private static class MessageSendResult {
        private final LocalMessageVo message;
        private boolean success;
        private String error;
        private LocalMessageVo sentMessage;

        public MessageSendResult(LocalMessageVo message) {
            this.message = message;
        }
    }
}