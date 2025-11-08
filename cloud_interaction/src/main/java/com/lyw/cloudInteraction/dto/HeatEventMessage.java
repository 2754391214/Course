package com.lyw.cloudInteraction.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 热度事件消息
 */
@Data
@Accessors(chain = true)
public class HeatEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String eventType; // LIKE, UNLIKE, USEFUL, UNUSEFUL, FAVORITE, etc.
    private Long courseId;
    private Long userId;
    private Long teacherId;
    private String targetType; // 添加这个字段：review, reply, course 等
    private Long targetId;
    private Double ratingValue; // 仅评分事件使用
    private String action; // ADD, REMOVE
    private Date eventTime;
    private Object eventData; // 扩展数据
}