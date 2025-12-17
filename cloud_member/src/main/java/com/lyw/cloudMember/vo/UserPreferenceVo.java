package com.lyw.cloudMember.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
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
@TableName("user_preference")
public class UserPreferenceVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("主题偏好")
    @TableField(value = "theme")
    private String theme;

    @ApiModelProperty("语言偏好")
    @TableField(value = "language")
    private String language;

    @ApiModelProperty("邮件通知")
    @TableField(value = "notification_email")
    private Integer notificationEmail;

    @ApiModelProperty("短信通知")
    @TableField(value = "notification_sms")
    private Integer notificationSms;

    @ApiModelProperty("推送通知")
    @TableField(value = "notification_push")
    private Integer notificationPush;

    @ApiModelProperty("隐私级别")
    @TableField(value = "privacy_level")
    private Integer privacyLevel;

    @ApiModelProperty("课程推荐")
    @TableField(value = "course_recommendation")
    private Integer courseRecommendation;

}
