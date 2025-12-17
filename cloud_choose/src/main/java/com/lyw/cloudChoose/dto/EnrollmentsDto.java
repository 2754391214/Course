package com.lyw.cloudChoose.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 选课表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
public class EnrollmentsDto extends BaseDto {

@ApiModelProperty("主键ID")
private Long id;
    
@ApiModelProperty("学生ID，关联用户表")
private Long studentId;
    
@ApiModelProperty("课程ID，关联课程表")
private Long courseId;
    
@ApiModelProperty("选课类型：NORMAL-正常选课, AUDIT-旁听, WAITLIST-等待列表")
private String enrollmentType;
    
@ApiModelProperty("选课批准时间")
@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
private Date approvedAt;
    
@ApiModelProperty("退课时间")
@DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
private Date droppedAt;
    
@ApiModelProperty("扩展字段，例如选课时的备注、原因等")
private String metadata;
    
@ApiModelProperty("优先级，用于抽签规则")
private Integer priority;
    
@ApiModelProperty("抽签结果：WIN-中签, LOSE-未中签")
private String lotteryResult;

@ApiModelProperty("学期")
private String semester;
}
