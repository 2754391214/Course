package com.lyw.commonUtil.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户行为消息
 */
@Data
@Accessors(chain = true)
public class UserBehaviorMessage implements Serializable {

    private static final long serialVersionUID = 1L;
    private String messageId;
    private Long userId;
    private Long courseId;
    /**
     * 行为类型
     */
    private String behaviorType;
    /**
     * 行为时间
     */
    private Date behaviorTime;
}