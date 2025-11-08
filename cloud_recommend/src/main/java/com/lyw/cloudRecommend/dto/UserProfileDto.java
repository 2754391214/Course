package com.lyw.cloudRecommend.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户画像表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
public class UserProfileDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("兴趣标签")
    private String interestTags;
    
    @ApiModelProperty("行为模式")
    private String behaviorPattern;
    
    @ApiModelProperty("偏好分类")
    private String preferredCategories;
    
    @ApiModelProperty("偏好难度")
    private String preferredDifficulty;
    
    @ApiModelProperty("偏好教师")
    private String preferredTeachers;
    
    @ApiModelProperty("学习目标")
    private String learningGoals;
    
    @ApiModelProperty("特征向量")
    private String featureVector;
                    
}
