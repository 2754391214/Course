package com.lyw.cloudCourse.service;

import com.lyw.cloudCourse.dto.TransactionCallbackDto;
import com.lyw.cloudCourse.feign.EnrollmentCallbackFeign;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnrollmentCallbackService {

    @Autowired
    private EnrollmentCallbackFeign enrollmentCallbackFeign;

    /**
     * 通知选课成功
     */
    public void notifyEnrollmentSuccess(String transactionId, String responseData) {
        try {
            TransactionCallbackDto callback = new TransactionCallbackDto();
            callback.setTransactionId(transactionId);
            callback.setResponseData(responseData);

            CourseResponseWrapper result = enrollmentCallbackFeign.notifySuccess(callback);

            if (result.isSuccess()) {
                log.info("选课成功回调成功: transactionId={}", transactionId);
            } else {
                log.error("选课成功回调失败: transactionId={}, message={}",
                        transactionId, result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("选课成功回调异常: transactionId={}", transactionId, e);
        }
    }

    /**
     * 通知选课失败
     */
    public void notifyEnrollmentFailure(String transactionId, String errorMessage) {
        try {
            TransactionCallbackDto callback = new TransactionCallbackDto();
            callback.setTransactionId(transactionId);
            callback.setErrorMessage(errorMessage);

            CourseResponseWrapper result = enrollmentCallbackFeign.notifyFailure(callback);

            if (result.isSuccess()) {
                log.info("选课失败回调成功: transactionId={}", transactionId);
            } else {
                log.error("选课失败回调失败: transactionId={}, message={}",
                        transactionId, result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("选课失败回调异常: transactionId={}", transactionId, e);
        }
    }
}