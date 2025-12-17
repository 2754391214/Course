package com.lyw.cloudChoose.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 选课策略配置表，支持多种选课算法和规则
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
public class EnrollmentStrategiesDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;

    @ApiModelProperty("课程ID")
    private Long courseId;

    @ApiModelProperty("策略类型：LOTTERY-抽签, FIRST_COME-先到先得, PRIORITY_BASED-优先级, HYBRID-混合")
    private String strategyType;

    @ApiModelProperty("退课策略:WAITLIST_DROP-等待队列补上， IMMEDIATE_DROP-立即退课")
    private String dropStrategyType;

    @ApiModelProperty("优先级规则配置，JSON格式定义各因素权重")
    private String priorityRules;

    @ApiModelProperty("抽签执行时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date lotteryTime;

    @ApiModelProperty("是否自动加入等待列表")
    private Boolean autoWaitlist;

    @ApiModelProperty("是否允许旁听")
    private Boolean allowAudit;

    @ApiModelProperty("是否检查时间冲突")
    private Boolean conflictCheck;

    @ApiModelProperty("是否检查先修课程")
    private Boolean prerequisiteCheck;

}
