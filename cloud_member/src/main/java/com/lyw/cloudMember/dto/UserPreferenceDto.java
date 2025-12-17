package com.lyw.cloudMember.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 用户偏好表
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Data
public class UserPreferenceDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("用户ID")
    private Long userId;
    
    @ApiModelProperty("主题偏好")
    private String theme;
    
    @ApiModelProperty("语言偏好")
    private String language;
    
    @ApiModelProperty("邮件通知")
    private Integer notificationEmail;
    
    @ApiModelProperty("短信通知")
    private Integer notificationSms;
    
    @ApiModelProperty("推送通知")
    private Integer notificationPush;
    
    @ApiModelProperty("隐私级别")
    private Integer privacyLevel;
    
    @ApiModelProperty("课程推荐")
    private Integer courseRecommendation;
                    
}
