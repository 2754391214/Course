package com.lyw.cloudCourse.dto;

import com.lyw.commonUtil.dto.BaseDto;
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
public class CourseCategoriesDto extends BaseDto {

    private Long id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类代码")
    private String code;

    @ApiModelProperty("排序")
    private Integer sortOrder;

}
