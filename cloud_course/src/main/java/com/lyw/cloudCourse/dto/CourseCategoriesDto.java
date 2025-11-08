package com.lyw.cloudCourse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
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
public class CourseCategoriesDto extends BaseDto {

    private Long id;

    @ApiModelProperty("分类名称")
    private String name;

    @ApiModelProperty("分类代码")
    private String code;

    @ApiModelProperty("父分类ID")
    private Long parentId;

    @ApiModelProperty("排序")
    private Integer sortOrder;

    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

}
