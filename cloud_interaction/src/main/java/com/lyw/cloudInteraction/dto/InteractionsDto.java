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
 * 互动表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@Accessors(chain = true)
public class InteractionsDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("用户ID，关联用户表")
    private Long userId;
    
    @ApiModelProperty("互动目标类型：course-课程,review-评价,teacher-教师")
    private String targetType;
    
    @ApiModelProperty("目标ID，根据target_type对应不同表的ID")
    private Long targetId;
    
    @ApiModelProperty("互动类型：like-点赞,favorite-收藏,useful-有用,share-分享,follow-关注,report-举报")
    private String interactionType;
    
    @ApiModelProperty("扩展字段，例如收藏夹名称等")
    private String metadata;
                    
}
