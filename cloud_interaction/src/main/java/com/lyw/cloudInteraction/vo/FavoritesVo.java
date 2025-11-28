package com.lyw.cloudInteraction.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <p>
 * 收藏夹表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@TableName("favorites")
public class FavoritesVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("收藏夹名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("收藏夹描述")
    @TableField(value = "description")
    private String description;

    @ApiModelProperty("是否公开")
    @TableField(value = "is_public")
    private Boolean isPublic;

    @ApiModelProperty("收藏夹封面")
    @TableField(value = "cover_image")
    private String coverImage;

    @ApiModelProperty("收藏项")
    @TableField(exist = false)
    private List<FavoriteItemsVo> favoriteItems;
}
