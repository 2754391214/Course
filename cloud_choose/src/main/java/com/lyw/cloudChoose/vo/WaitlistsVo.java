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
 * 选课等待列表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@TableName("waitlists")
public class WaitlistsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("学生ID")
    @TableField(value = "student_id")
    private Long studentId;

    @ApiModelProperty("课程ID")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("在等待列表中的位置")
    @TableField(value = "position")
    private Integer position;

    @ApiModelProperty("加入等待列表时间")
    @TableField(value = "joined_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date joinedAt;

    @ApiModelProperty("状态：WAITING-等待中, OFFERED-已提供席位, EXPIRED-已过期, CANCELLED-已取消")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("席位过期时间（当状态为OFFERED时）")
    @TableField(value = "expires_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date expiresAt;

    @ApiModelProperty("优先级，用于抽签规则")
    @TableField(value = "priority")
    private Integer priority;

    @ApiModelProperty("抽签结果：WIN-中签, LOSE-未中签")
    @TableField(value = "lottery_result")
    private String lotteryResult;

}
