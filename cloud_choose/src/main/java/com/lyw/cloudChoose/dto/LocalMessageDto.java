package com.lyw.cloudChoose.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 本地消息表
 * </p>
 *
 * @author lyw
 * @since 2025/11/19
 */
@Data
public class LocalMessageDto extends BaseDto {
    
    @ApiModelProperty("业务唯一键，主键ID，保证唯一")
    private String businessKey;
    
    @ApiModelProperty("RabbitMQ交换机名称")
    private String exchange;
    
    @ApiModelProperty("RabbitMQ路由键")
    private String routingKey;
    
    @ApiModelProperty("消息体，JSON格式，包含原事务记录的业务数据")
    private String messageBody;
    
    @ApiModelProperty("消息状态：0-待发送, 1-已发送, 2-发送失败")
    private Integer status;
    
    @ApiModelProperty("重试次数")
    private Integer retryCount;
    
    @ApiModelProperty("最大重试次数")
    private Integer maxRetryCount;
    
    @ApiModelProperty("下次重试时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date nextRetryTime;
    
    @ApiModelProperty("错误信息，记录发送失败的原因")
    private String errorMessage;
                    
}
