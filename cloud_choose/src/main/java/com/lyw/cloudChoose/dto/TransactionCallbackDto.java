package com.lyw.cloudChoose.dto;

import lombok.Data;

/**
 * 事务回调DTO
 */
@Data
public class TransactionCallbackDto {
    private String transactionId;
    private String responseData;
    private String errorMessage;
}
