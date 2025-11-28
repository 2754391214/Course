package com.lyw.cloudInteraction.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("likes")
public class LikesVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("目标类型：course-课程,review-评价")
    @TableField(value = "target_type")
    private String targetType;

    @ApiModelProperty("目标ID")
    @TableField(value = "target_id")
    private Long targetId;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

}
