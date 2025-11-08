package com.lyw.commonUtil.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 选课消息
 */
@Data
@Accessors(chain = true)
public class EnrollmentMessage implements Serializable {
    private String transactionId;
    private Long courseId;
    private String operation; // INCREMENT_ENROLLMENT, DECREMENT_ENROLLMENT
    private Long timestamp;
    private Integer newEnrollmentCount; // 新的选课人数
}
