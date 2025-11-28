package com.lyw.cloudCourse.consumer;


import com.lyw.cloudCourse.service.CoursesBo;
import com.lyw.commonUtil.annotation.MessageIdempotent;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import com.lyw.commonUtil.message.EnrollmentMessage;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CourseEnrollmentConsumer {

    @Autowired
    private CoursesBo coursesService;

    /**
     * 消费选课消息
     */
    @RabbitListener(queues = RabbitmqKeyConstant.ENROLLMENT_QUEUE)
    @MessageIdempotent(
            key = "#message.messageId+':'+#message.operation",  // 使用事务ID作为幂等键
            expire = 600,                   // 10分钟过期
            handleType = MessageIdempotent.HandleType.RETURN_NULL
    )
    public void processEnrollmentMessage(EnrollmentMessage message,
                                         Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String transactionId = message.getMessageId();

        try {
            log.info("收到选课消息: transactionId={}, courseId={}, operation={}", transactionId, message.getCourseId(), message.getOperation());

            if ("INCREMENT_ENROLLMENT".equals(message.getOperation())) {
                // 调用课程服务增加选课人数
                CourseResponseWrapper result = coursesService.incrementEnrollment(message.getCourseId());

                if (result.isSuccess()) {
                    log.info("课程人数增加成功: transactionId={}, courseId={}", transactionId, message.getCourseId());

                    // 手动确认消息
                    channel.basicAck(deliveryTag, false);

                } else {
                    throw new RuntimeException("课程服务返回失败: " + result.getErrorMessage());
                }

            } else if ("DECREMENT_ENROLLMENT".equals(message.getOperation())) {
                // 退课 - 减少课程选课人数
                CourseResponseWrapper result = coursesService.decrementEnrollment(message.getCourseId());

                if (result.isSuccess()) {
                    log.info("课程人数减少成功: transactionId={}, courseId={}", transactionId, message.getCourseId());

                    channel.basicAck(deliveryTag, false);
                } else {
                    throw new RuntimeException("课程服务返回失败: " + result.getErrorMessage());
                }
            }

        } catch (Exception e) {
            log.error("处理选课消息异常: transactionId={}", transactionId, e);
            try {

                // 拒绝消息并重新入队（重试）
                channel.basicNack(deliveryTag, false, true);

            } catch (IOException ioException) {
                log.error("拒绝消息异常: transactionId={}", transactionId, ioException);
            }
        }
    }

    /**
     * 消费死信队列消息（处理失败的消息）
     */
    @RabbitListener(queues = RabbitmqKeyConstant.ENROLLMENT_DLX_QUEUE)
    public void processDLXMessage(EnrollmentMessage message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        String transactionId = message.getMessageId();

        try {
            log.warn("收到死信队列消息: transactionId={}, courseId={}",
                    transactionId, message.getCourseId());

            // 确认死信消息
            channel.basicAck(deliveryTag, false);

            log.info("死信消息处理完成: transactionId={}", transactionId);

        } catch (Exception e) {
            log.error("处理死信消息异常: transactionId={}", transactionId, e);
        }
    }
}