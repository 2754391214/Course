package com.lyw.cloudChoose.service.impl;

import com.lyw.cloudChoose.dto.TransactionCallbackDto;
import com.lyw.cloudChoose.service.EnrollmentCallBackBo;
import com.lyw.cloudChoose.service.EnrollmentTransationBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class EnrollmentCallBackImpl implements EnrollmentCallBackBo {
    @Resource
    private EnrollmentTransationBo transactionService;

    @Override
    public CourseResponseWrapper handleSuccess(TransactionCallbackDto callback) {
        try {
            log.info("收到事务成功回调: transactionId={}", callback.getTransactionId());

            // 更新事务状态为成功
            transactionService.updateTransactionStatus(
                    callback.getTransactionId(),
                    "SUCCESS",
                    callback.getResponseData(),
                    null
            );

            log.info("事务状态更新为成功: transactionId={}", callback.getTransactionId());
            return CourseResponseWrapper.getSuccess("处理成功");

        } catch (Exception e) {
            log.error("处理成功回调异常: transactionId={}", callback.getTransactionId(), e);
            return CourseResponseWrapper.getFailed("处理失败");
        }
    }

    @Override
    public CourseResponseWrapper handleFailure(TransactionCallbackDto callback) {
        try {
            log.error("收到事务失败回调: transactionId={}, error={}",
                    callback.getTransactionId(), callback.getErrorMessage());

            // 更新事务状态为失败
            transactionService.updateTransactionStatus(
                    callback.getTransactionId(),
                    "FAILED",
                    null,
                    callback.getErrorMessage()
            );

            // 执行补偿操作（回滚选课记录等）
            transactionService.compensateFailedTransaction(callback.getTransactionId());

            log.info("事务状态更新为失败并执行补偿: transactionId={}", callback.getTransactionId());
            return CourseResponseWrapper.getSuccess("处理成功");

        } catch (Exception e) {
            log.error("处理失败回调异常: transactionId={}", callback.getTransactionId(), e);
            return CourseResponseWrapper.getFailed("处理失败");
        }
    }
}
