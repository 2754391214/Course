package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.cloudChoose.vo.EnrollmentsVo;

public interface EnrollmentTransationBo {
    /**
     * 创建选课事务记录
     */
    String createEnrollmentTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment);
    /**
     * 异步增加课程选课人数
     */
    void asyncIncrementCourseEnrollment(Long courseId, String transactionId);
    /**
     * 更新事务状态
     */
    void updateTransactionStatus(String transactionId, String status, String responseData, String errorMessage);
    /**
     * 补偿失败的事务
     */
    void compensateFailedTransaction(String transactionId);
    /**
     * 批量补偿失败的事务
     */
    void batchCompensateFailedTransactions();
    /**
     * 创建退选事务记录
     */
    String createDropTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment);
    /**
     * 异步减少课程选课人数
     */
    void asyncDecrementCourseEnrollment(Long courseId, String transactionId);
    /**
     * 创建等待列表选课事务记录
     */
    String createWaitlistEnrollmentTransaction(EnrollmentsDto dto, EnrollmentsVo enrollment);
}
