package com.lyw.cloudChoose.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 本地消息表
 * </p>
 */
@Data
@Accessors(chain = true)
@TableName("local_message")
public class LocalMessageVo extends BaseVo {

    @ApiModelProperty("业务唯一键，主键ID")
    @TableId(value = "business_key")
    private String businessKey;

    @ApiModelProperty("消息类型：ENROLLMENT-选课, DROP-退课")
    @TableField(value = "message_type")
    private String messageType;

    @ApiModelProperty("RabbitMQ交换机名称")
    @TableField(value = "exchange")
    private String exchange;

    @ApiModelProperty("RabbitMQ路由键")
    @TableField(value = "routing_key")
    private String routingKey;

    @ApiModelProperty("消息体，JSON格式，包含业务数据")
    @TableField(value = "message_body")
    private String messageBody;

    @ApiModelProperty("消息状态：0-待发送, 1-已发送, 2-发送失败, 3-最终失败")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty("重试次数")
    @TableField(value = "retry_count")
    private Integer retryCount = 0;

    @ApiModelProperty("最大重试次数")
    @TableField(value = "max_retry_count")
    private Integer maxRetryCount = 5;

    @ApiModelProperty("下次重试时间")
    @TableField(value = "next_retry_time")
    private Date nextRetryTime;

    @ApiModelProperty("错误信息，记录发送失败的原因")
    @TableField(value = "error_message")
    private String errorMessage;

    // 状态常量
    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_SENT = 1;
    public static final Integer STATUS_FAILED = 2;
    public static final Integer STATUS_FINAL_FAILURE = 3;

    // 消息类型常量
    public static final String TYPE_COURSE_INCREMENT = "COURSE_INCREMENT";
    public static final String TYPE_COURSE_DECREMENT = "COURSE_DECREMENT";
}