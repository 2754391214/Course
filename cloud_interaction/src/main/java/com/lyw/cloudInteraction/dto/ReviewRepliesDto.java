package com.lyw.cloudInteraction.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 评价回复表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
public class ReviewRepliesDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("评价ID")
    private Long reviewId;
    
    @ApiModelProperty("父回复ID，用于回复的回复")
    private Long parentId;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("回复内容")
    private String content;
    
    @ApiModelProperty("状态：pending-待审核,approved-已审核,rejected-已拒绝,hidden-已隐藏")
    private String status;
    
    @ApiModelProperty("是否匿名")
    private Boolean anonymous;

    @ApiModelProperty("扩展字段")
    private String metadata;
}
