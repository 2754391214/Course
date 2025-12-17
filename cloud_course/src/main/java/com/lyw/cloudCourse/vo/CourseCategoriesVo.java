package com.lyw.cloudCourse.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 课程分类表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
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

    @ApiModelProperty("排序")
    @TableField(value = "sort_order")
    private Integer sortOrder;

}
