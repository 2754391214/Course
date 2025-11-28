package com.lyw.cloudChoose.task;

import com.lyw.cloudChoose.service.LocalMessageService;
import com.lyw.cloudChoose.vo.LocalMessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class LocalMessageRetryJob {

    @Resource
    private LocalMessageService localMessageService;

    /**
     * 每30秒执行一次，重试发送失败的消息
     */
    @Scheduled(fixedRate = 30000)
    public void retryFailedMessages() {
        try {
            List<LocalMessageVo> pendingMessages = localMessageService.getPendingRetryMessages();
            if (pendingMessages.isEmpty()) {
                return;
            }

            log.info("开始重试发送失败的消息，数量: {}", pendingMessages.size());

            for (LocalMessageVo message : pendingMessages) {
                try {
                    boolean success = localMessageService.sendMessage(message);
                    if (success) {
                        log.info("消息重试发送成功: businessKey={}", message.getBusinessKey());
                    }
                } catch (Exception e) {
                    log.error("消息重试发送异常: businessKey={}", message.getBusinessKey(), e);
                }
            }

            log.info("消息重试任务完成，处理数量: {}", pendingMessages.size());

        } catch (Exception e) {
            log.error("消息重试任务执行异常", e);
        }
    }
}