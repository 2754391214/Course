package com.lyw.cloudChoose.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@Data
@Accessors(chain = true)
public class EnrollmentBlacklistDto extends BaseDto {

    @ApiModelProperty("黑名单记录ID，主键自增，唯一标识每条黑名单记录")
    private Long id;
    
    @ApiModelProperty("学生ID，关联用户表，标识被加入黑名单的学生")
    private Long studentId;
    
    @ApiModelProperty("课程ID，关联课程表，NULL表示全局黑名单（所有课程受限），非NULL表示特定课程黑名单")
    private Long courseId;
    
    @ApiModelProperty("黑名单类型：GLOBAL-全局黑名单（所有课程受限）, COURSE-课程黑名单（特定课程受限）, TEMPORARY-临时限制")
    private String blacklistType;
    
    @ApiModelProperty("加入黑名单的原因描述，如：恶意退课、违规行为、欠费等")
    private String reason;
    
    @ApiModelProperty("原因编码，用于系统识别：MALICIOUS_DROP-恶意退课, VIOLATION-违规行为, FEE_ARREARS-欠费, ACADEMIC_PROBATION-学业警告, OTHER-其他")
    private String reasonCode;
    
    @ApiModelProperty("黑名单状态：ACTIVE-生效中, INACTIVE-已失效, EXPIRED-已过期, CANCELLED-已取消")
    private String status;
    
    @ApiModelProperty("限制级别：ENROLLMENT_BLOCK-禁止选课, DROP_BLOCK-禁止退课, READ_ONLY-只读权限, WARNING-仅警告")
    private String restrictionLevel;
    
    @ApiModelProperty("黑名单生效开始时间")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date startTime;
    
    @ApiModelProperty("黑名单结束时间，NULL表示永久有效")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    
    @ApiModelProperty("是否自动释放：0-手动释放，1-到期自动释放")
    private Boolean autoRelease;
    
    @ApiModelProperty("自动释放条件描述，如：缴费后自动解除、补考通过后解除等")
    private String releaseCondition;
                    
    @ApiModelProperty("备注信息，存储额外的说明或操作记录")
    private String remarks;
    
    @ApiModelProperty("扩展元数据，JSON格式存储额外信息，如：操作日志、附件信息等")
    private String metadata;
    
}
