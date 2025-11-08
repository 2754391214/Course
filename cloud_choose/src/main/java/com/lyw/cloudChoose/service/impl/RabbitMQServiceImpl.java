package com.lyw.cloudChoose.service.impl;

import com.lyw.cloudChoose.config.RabbitMQConfig;
import com.lyw.cloudChoose.service.RabbitMQService;
import com.lyw.commonUtil.dto.EnrollmentMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class RabbitMQServiceImpl implements RabbitMQService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送选课消息
     */
    public void sendEnrollmentMessage(EnrollmentMessage message) {
        try {
            CorrelationData correlationData = new CorrelationData(message.getTransactionId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ENROLLMENT_EXCHANGE,
                    RabbitMQConfig.ENROLLMENT_ROUTING_KEY,
                    message,
                    correlationData
            );

            log.info("选课消息发送成功: transactionId={}, courseId={}",
                    message.getTransactionId(), message.getCourseId());

        } catch (Exception e) {
            log.error("选课消息发送失败: transactionId={}, courseId={}",
                    message.getTransactionId(), message.getCourseId(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    /**
     * 发送延迟消息（用于重试）
     */
    public void sendDelayedEnrollmentMessage(EnrollmentMessage message, long delayMillis) {
        try {
            // 设置消息的过期时间 - 明确指定参数类型
            MessagePostProcessor processor = (Message amqpMessage) -> {
                amqpMessage.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                return amqpMessage;
            };

            CorrelationData correlationData = new CorrelationData(message.getTransactionId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ENROLLMENT_EXCHANGE,
                    RabbitMQConfig.ENROLLMENT_ROUTING_KEY,
                    message,
                    processor,
                    correlationData
            );

            log.info("延迟选课消息发送成功: transactionId={}, delay={}ms",
                    message.getTransactionId(), delayMillis);

        } catch (Exception e) {
            log.error("延迟选课消息发送失败: transactionId={}", message.getTransactionId(), e);
            throw new RuntimeException("延迟消息发送失败", e);
        }
    }
}