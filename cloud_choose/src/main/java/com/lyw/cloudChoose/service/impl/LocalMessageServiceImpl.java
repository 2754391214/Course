package com.lyw.cloudChoose.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudChoose.mapper.LocalMessageDao;
import com.lyw.cloudChoose.vo.LocalMessageVo;
import com.lyw.cloudChoose.service.LocalMessageService;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LocalMessageServiceImpl extends ServiceImpl<LocalMessageDao, LocalMessageVo> implements LocalMessageService {

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * 批量更新消息
     */
    @Override
    public boolean updateBatchByIds(List<LocalMessageVo> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return true;
        }

        try {
            // 分批处理，避免单次批量过大
            List<List<LocalMessageVo>> partitions = Lists.partition(messages, 100);

            for (List<LocalMessageVo> batch : partitions) {
                // 使用 MyBatis-Plus 的批量更新
                this.updateBatchById(batch);
            }

            log.debug("批量更新消息完成: 数量={}", messages.size());
            return true;

        } catch (Exception e) {
            log.error("批量更新消息失败", e);
            return false;
        }
    }
    /**
     * 创建本地消息
     */
    @Override
    public LocalMessageVo createMessage(String messageType, String routingKey, Object messageBody, String exchange, String currentDateTime, String userId,String messageId) {
        LocalMessageVo message = new LocalMessageVo()
                .setBusinessKey(messageId)
                .setMessageType(messageType)
                .setExchange(exchange)
                .setRoutingKey(routingKey)
                .setMessageBody(JSON.toJSONString(messageBody))
                .setStatus(LocalMessageVo.STATUS_PENDING)
                .setRetryCount(0)
                .setMaxRetryCount(5);

        // 设置创建时间和更新时间为当前时间
        message.setCrdAndLud(currentDateTime);
        message.setCruAndLuu(userId);
        return message;
    }

    /**
     * 发送消息到RabbitMQ
     */
    @Override
    public boolean sendMessage(LocalMessageVo message) {
        try {
            Object body = JSON.parse(message.getMessageBody());
            rabbitTemplate.convertAndSend(
                    message.getExchange(),
                    message.getRoutingKey(),
                    body
            );

            // 更新消息状态为已发送
            message.setStatus(LocalMessageVo.STATUS_SENT);
            message.setLud(DateTimeUtils.getCurrentDateTime());
            this.updateById(message);

            log.info("消息发送成功: businessKey={}", message.getBusinessKey());
            return true;

        } catch (Exception e) {
            log.error("消息发送失败: businessKey={}", message.getBusinessKey(), e);

            // 更新消息状态和错误信息
            int newRetryCount = message.getRetryCount() + 1;
            message.setRetryCount(newRetryCount);
            message.setErrorMessage(e.getMessage());

            if (newRetryCount >= message.getMaxRetryCount()) {
                message.setStatus(LocalMessageVo.STATUS_FINAL_FAILURE);
                log.error("消息达到最大重试次数，标记为最终失败: businessKey={}", message.getBusinessKey());
            } else {
                message.setStatus(LocalMessageVo.STATUS_FAILED);
                // 设置下次重试时间（指数退避）
                message.setNextRetryTime(calculateNextRetryTime(newRetryCount));
            }

            message.setLud(DateTimeUtils.getCurrentDateTime());
            this.updateById(message);
            return false;
        }
    }

    /**
     * 获取待重试的消息
     */
    @Override
    public List<LocalMessageVo> getPendingRetryMessages() {
        Date now = new Date();
        QueryWrapper<LocalMessageVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", LocalMessageVo.STATUS_PENDING, LocalMessageVo.STATUS_FAILED)
                .and(wrapper -> wrapper.apply("retry_count < max_retry_count OR max_retry_count IS NULL"))
                .and(wrapper -> wrapper.isNull("next_retry_time")
                        .or()
                        .le("next_retry_time", now)
        );
        return this.list(queryWrapper);
    }

    @Override
    public void batchInsertMessages(List<LocalMessageVo> batch) {
        baseMapper.batchInsertMessages(batch);
    }

    private String generateBusinessKey(String messageType) {
        return messageType + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private Date calculateNextRetryTime(int retryCount) {
        // 指数退避算法：1s, 5s, 15s, 30s, 60s
        long delaySeconds = Math.min(60, (long) Math.pow(2, retryCount) * 1000);
        return new Date(System.currentTimeMillis() + delaySeconds);
    }
}