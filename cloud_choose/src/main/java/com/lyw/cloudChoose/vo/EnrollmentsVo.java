package com.lyw.cloudChoose.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.cloudChoose.dto.StudentProfileDto;
import com.lyw.commonUtil.vo.BaseVo;
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

    @ApiModelProperty("课程信息")
    @TableField(exist = false)
    private CoursesDto courses;

    @ApiModelProperty("学生信息")
    @TableField(exist = false)
    private StudentProfileDto studentProfile;
}
