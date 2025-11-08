package com.lyw.cloudCourse.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 院系信息表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
@Accessors(chain = true)
public class DepartmentsDto extends BaseDto {

    @ApiModelProperty("院系ID，主键自增")
    private Long id;

    @ApiModelProperty("院系代码，唯一标识")
    private String code;

    @ApiModelProperty("院系名称")
    private String name;

    @ApiModelProperty("院系描述")
    private String description;

    @ApiModelProperty("父院系ID，支持院系层级结构")
    private Long parentId;

    @ApiModelProperty("排序字段，用于控制显示顺序")
    private Integer sortOrder;

    @ApiModelProperty("院系状态：活跃/非活跃")
    private String status = "N";

}
