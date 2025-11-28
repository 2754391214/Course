package com.lyw.cloudInteraction.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 点赞表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class LikesDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("目标类型：course-课程,review-评价")
    private String targetType;
    
    @ApiModelProperty("目标ID")
    private Long targetId;

    @ApiModelProperty("用户ID")
    private Long userId;

    @ApiModelProperty("课程ID")
    private Long courseId;
}
