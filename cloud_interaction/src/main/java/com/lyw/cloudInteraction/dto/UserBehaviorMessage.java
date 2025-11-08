package com.lyw.cloudInteraction.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 用户行为消息DTO - 与互动模块保持一致
 */
@Data
@Accessors(chain = true)
public class UserBehaviorMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID（用于去重）
     */
    private String messageId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 课程ID
     */
    private Long courseId;

    /**
     * 行为类型: VIEW, ENROLL, COMPLETE, LIKE, RATE, SEARCH, SHARE, COMMENT
     */
    private String behaviorType;

    /**
     * 行为权重（可选，推荐模块可以自己计算）
     */
    private BigDecimal behaviorWeight;

    /**
     * 行为上下文信息
     */
    private Map<String, Object> context;

    /**
     * 行为时间
     */
    private Date behaviorTime;

    /**
     * 来源模块
     */
    private String sourceModule;

    /**
     * 消息创建时间
     */
    private Date createTime;

    /**
     * 业务版本号
     */
    private String version;

    public UserBehaviorMessage() {
        this.createTime = new Date();
        this.sourceModule = "interaction";
        this.version = "1.0";
    }
}