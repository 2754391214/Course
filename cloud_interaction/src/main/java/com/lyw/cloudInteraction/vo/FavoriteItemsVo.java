package com.lyw.cloudInteraction.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 收藏项表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@TableName("favorite_items")
public class FavoriteItemsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("收藏夹ID")
    @TableField(value = "favorite_id")
    private Long favoriteId;

    @ApiModelProperty("目标类型：course-课程,review-评价")
    @TableField(value = "target_type")
    private String targetType;

    @ApiModelProperty("目标ID")
    @TableField(value = "target_id")
    private Long targetId;

    @ApiModelProperty("收藏备注")
    @TableField(value = "notes")
    private String notes;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("是取消收藏")
    @TableField(exist = false)
    private Boolean cannelFavorite=true;

    @ApiModelProperty("移動到對於的收藏夾")
    @TableField(exist = false)
    private Long targetFavoriteId;
}
