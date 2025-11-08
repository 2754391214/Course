package com.lyw.cloudCourse.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * <p>
 * 课程分类表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
@TableName("course_categories")
public class CourseCategoriesVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("分类名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("分类代码")
    @TableField(value = "code")
    private String code;

    @ApiModelProperty("父分类ID")
    @TableField(value = "parent_id")
    private Long parentId;

    @ApiModelProperty("排序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

    @TableField(value = "created_at")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

}
