package com.lyw.cloudInteraction.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 收藏项表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class FavoriteItemsDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("收藏夹ID")
    private Long favoriteId;
    
    @ApiModelProperty("目标类型：course-课程,review-评价")
    private String targetType;
    
    @ApiModelProperty("目标ID")
    private Long targetId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("收藏备注")
    private String notes;

    @ApiModelProperty("移動到對於的收藏夾")
    private Long targetFavoriteId;

    @ApiModelProperty("课程ID")
    private Long courseId;
}
