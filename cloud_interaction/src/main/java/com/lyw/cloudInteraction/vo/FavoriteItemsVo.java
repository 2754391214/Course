package com.lyw.cloudInteraction.vo;

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
 * 收藏项表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@Accessors(chain = true)
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

}
