package com.lyw.cloudRecommend.consumer;

import com.lyw.cloudRecommend.dto.UserBehaviorMessage;
import com.lyw.cloudRecommend.service.RecommendService;
import com.lyw.commonUtil.constant.RabbitmqKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Component
public class UserBehaviorConsumer {
    @Resource
    private RecommendService recommendService;

    @RabbitListener(queues = RabbitmqKeyConstant.USER_BEHAVIOR_QUEUE)
    @Transactional
    public void handleUserBehaviorMessage(@Payload UserBehaviorMessage message) {
        recommendService.handleUserBehaviorMessage(message);
    }

    /**
     * 监听死信队列 - 处理失败的消息
     */
    @RabbitListener(queues = RabbitmqKeyConstant.USER_BEHAVIOR_DLQ_QUEUE)
    public void handleFailedUserBehaviorMessage(@Payload UserBehaviorMessage message) {
        recommendService.handleFailedUserBehaviorMessage(message);
    }
}
