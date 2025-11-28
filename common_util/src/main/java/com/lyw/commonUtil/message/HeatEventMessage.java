package com.lyw.commonUtil.message;

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

    private String messageId;
    private String eventType; //点赞 收藏 浏览 选课
    private Long courseId;
    private String targetType; //操作的是 评论 课程
    private Double ratingValue; // 仅评分事件使用
    private Date eventTime;
}