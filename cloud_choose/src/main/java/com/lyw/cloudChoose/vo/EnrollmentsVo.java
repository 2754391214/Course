package com.lyw.cloudChoose.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

/**
 * <p>
 * 选课表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
@TableName("enrollments")
public class EnrollmentsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("学生ID，关联用户表")
    @TableField(value = "student_id")
    private Long studentId;

    @ApiModelProperty("课程ID，关联课程表")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("选课类型：NORMAL-正常选课, AUDIT-旁听, WAITLIST-等待列表")
    @TableField(value = "enrollment_type")
    private String enrollmentType;

    @ApiModelProperty("选课状态：PENDING-待处理, SUCCESS-成功, FAILED-失败, DROPPED-已退课, WAITING-等待中")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("选课来源：WEB, APP, ADMIN等")
    @TableField(value = "enrollment_source")
    private String enrollmentSource;

    @ApiModelProperty("选课时间")
    @TableField(value = "enrolled_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date enrolledAt;

    @ApiModelProperty("选课批准时间")
    @TableField(value = "approved_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date approvedAt;

    @ApiModelProperty("退课时间")
    @TableField(value = "dropped_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date droppedAt;

    @ApiModelProperty("扩展字段，例如选课时的备注、原因等")
    @TableField(value = "metadata")
    private String metadata;

    @ApiModelProperty("优先级，用于抽签规则")
    @TableField(value = "priority")
    private Integer priority;

    @ApiModelProperty("抽签结果：WIN-中签, LOSE-未中签")
    @TableField(value = "lottery_result")
    private String lotteryResult;

}
