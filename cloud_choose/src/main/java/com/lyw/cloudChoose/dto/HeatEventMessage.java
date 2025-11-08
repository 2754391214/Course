package com.lyw.cloudChoose.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String eventType; // ENROLLMENT, WITHDRAWAL, RATING, LIKE, FAVORITE, COMMENT
    private Long courseId;
    private Long userId;
    private Long teacherId;
    private String targetType; // review, reply, course 等
    private Long targetId;
    private Double ratingValue; // 仅评分事件使用
    private String action; // ADD, REMOVE

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date eventTime;

    private Object eventData; // 扩展数据
}