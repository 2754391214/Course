package com.lyw.cloudInteraction.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 收藏项表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@Accessors(chain = true)
public class FavoriteItemsDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("收藏夹ID")
    private Long favoriteId;
    
    @ApiModelProperty("目标类型：course-课程,review-评价")
    private String targetType;
    
    @ApiModelProperty("目标ID")
    private Long targetId;
    
    @ApiModelProperty("收藏备注")
    private String notes;
                    
}
