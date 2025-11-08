package com.lyw.cloudMember.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * 教师信息表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
@Accessors(chain = true)
@TableName("teacher_profile")
public class TeacherProfileVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("工号")
    @TableField(value = "teacher_id")
    private String teacherId;

    @ApiModelProperty("职称")
    @TableField(value = "title")
    private String title;

    @ApiModelProperty("部门")
    @TableField(value = "department")
    private String department;

    @ApiModelProperty("办公室")
    @TableField(value = "office")
    private String office;

    @ApiModelProperty("研究方向")
    @TableField(value = "research_field")
    private String researchField;

    @ApiModelProperty("教龄")
    @TableField(value = "teaching_years")
    private Integer teachingYears;

    @ApiModelProperty("是否是导师")
    @TableField(value = "is_tutor")
    private Integer isTutor;

    @ApiModelProperty("任职状态:1-在职 2-离职 3-退休")
    @TableField(value = "employment_status")
    private Integer employmentStatus;

}
