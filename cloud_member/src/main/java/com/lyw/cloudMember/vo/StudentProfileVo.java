package com.lyw.cloudMember.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 学生信息表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
@TableName("student_profile")
public class StudentProfileVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("姓名")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("相片")
    @TableField(value = "avatar")
    private String avatar;

    @ApiModelProperty("学号")
    @TableField(value = "student_id")
    private String studentId;

    @ApiModelProperty("年级")
    @TableField(value = "grade")
    private String grade;

    @ApiModelProperty("专业")
    @TableField(value = "major")
    private String major;

    @ApiModelProperty("学院")
    @TableField(value = "faculty")
    private String faculty;

    @ApiModelProperty("入学年份")
    @TableField(value = "enrollment_year")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date enrollmentYear;

    @ApiModelProperty("预计毕业年份")
    @TableField(value = "expected_graduation")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date expectedGraduation;

    @ApiModelProperty("绩点")
    @TableField(value = "gpa")
    private BigDecimal gpa;

    @ApiModelProperty("已获学分")
    @TableField(value = "credits_earned")
    private Integer creditsEarned;

    @ApiModelProperty("学业状态:1-在读 2-休学 3-毕业")
    @TableField(value = "academic_status")
    private Integer academicStatus;

}
