package com.lyw.cloudChoose.service;

import com.lyw.commonUtil.dto.EnrollmentMessage;

public interface RabbitMQService {

    /**
     * 发送选课消息
     */
    void sendEnrollmentMessage(EnrollmentMessage message);
    /**
     * 发送延迟消息（用于重试）
     */
    void sendDelayedEnrollmentMessage(EnrollmentMessage message, long delayMillis);
}