package com.lyw.cloudRecommend.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("user_profile")
public class UserProfileVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("兴趣标签")
    @TableField(value = "interest_tags")
    private String interestTags;

    @ApiModelProperty("行为模式")
    @TableField(value = "behavior_pattern")
    private String behaviorPattern;

    @ApiModelProperty("偏好分类")
    @TableField(value = "preferred_categories")
    private String preferredCategories;

    @ApiModelProperty("偏好难度")
    @TableField(value = "preferred_difficulty")
    private String preferredDifficulty;

    @ApiModelProperty("偏好教师")
    @TableField(value = "preferred_teachers")
    private String preferredTeachers;

    @ApiModelProperty("学习目标")
    @TableField(value = "learning_goals")
    private String learningGoals;

    @ApiModelProperty("特征向量")
    @TableField(value = "feature_vector")
    private String featureVector;

}
