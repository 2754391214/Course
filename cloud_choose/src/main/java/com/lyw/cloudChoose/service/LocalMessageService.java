package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.vo.LocalMessageVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 本地消息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/19
 */
public interface LocalMessageService extends IService<LocalMessageVo> {
    boolean updateBatchByIds(List<LocalMessageVo> messages);
    LocalMessageVo createMessage(String messageType, String routingKey, Object messageBody, String exchange, String currentDateTime, String userId,String messageId);
    boolean sendMessage(LocalMessageVo message);
    List<LocalMessageVo> getPendingRetryMessages();
    void batchInsertMessages(List<LocalMessageVo> batch);
}
