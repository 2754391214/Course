package com.lyw.cloudChoose.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@Data
@TableName("enrollment_blacklist")
public class EnrollmentBlacklistVo extends BaseVo {

    @ApiModelProperty("黑名单记录ID，主键自增，唯一标识每条黑名单记录")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("学生ID，关联用户表，标识被加入黑名单的学生")
    @TableField(value = "student_id")
    private Long studentId;

    @ApiModelProperty("课程ID，关联课程表，NULL表示全局黑名单（所有课程受限），非NULL表示特定课程黑名单")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("黑名单类型：GLOBAL-全局黑名单（所有课程受限）, COURSE-课程黑名单（特定课程受限）, TEMPORARY-临时限制")
    @TableField(value = "blacklist_type")
    private String blacklistType;

    @ApiModelProperty("加入黑名单的原因描述，如：恶意退课、违规行为、欠费等")
    @TableField(value = "reason")
    private String reason;

    @ApiModelProperty("原因编码，用于系统识别：MALICIOUS_DROP-恶意退课, VIOLATION-违规行为, FEE_ARREARS-欠费, ACADEMIC_PROBATION-学业警告, OTHER-其他")
    @TableField(value = "reason_code")
    private String reasonCode;

    @ApiModelProperty("黑名单状态：ACTIVE-生效中, INACTIVE-已失效, EXPIRED-已过期, CANCELLED-已取消")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("限制级别：ENROLLMENT_BLOCK-禁止选课, DROP_BLOCK-禁止退课, READ_ONLY-只读权限, WARNING-仅警告")
    @TableField(value = "restriction_level")
    private String restrictionLevel;

    @ApiModelProperty("黑名单生效开始时间")
    @TableField(value = "start_time")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty("黑名单结束时间，NULL表示永久有效")
    @TableField(value = "end_time")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty("是否自动释放：0-手动释放，1-到期自动释放")
    @TableField(value = "auto_release")
    private Boolean autoRelease;

    @ApiModelProperty("自动释放条件描述，如：缴费后自动解除、补考通过后解除等")
    @TableField(value = "release_condition")
    private String releaseCondition;

    @ApiModelProperty("备注信息，存储额外的说明或操作记录")
    @TableField(value = "remarks")
    private String remarks;

    @ApiModelProperty("扩展元数据，JSON格式存储额外信息，如：操作日志、附件信息等")
    @TableField(value = "metadata")
    private String metadata;

}
