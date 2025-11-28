package com.lyw.cloudInteraction.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 收藏夹表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class FavoritesDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("收藏夹名称")
    private String name;
    
    @ApiModelProperty("收藏夹描述")
    private String description;
    
    @ApiModelProperty("是否公开")
    private Boolean isPublic;
    
    @ApiModelProperty("收藏夹封面")
    private String coverImage;
                    
}
