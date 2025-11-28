package com.lyw.commonUtil.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 选课消息
 */
@Data
@Accessors(chain = true)
public class EnrollmentMessage implements Serializable {
    private String messageId;
    private Long courseId;
    private String operation;
}
