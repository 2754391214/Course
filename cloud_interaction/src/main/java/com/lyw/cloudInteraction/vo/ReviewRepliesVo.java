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
 * 评价回复表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@TableName("review_replies")
public class ReviewRepliesVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("评价ID")
    @TableField(value = "review_id")
    private Long reviewId;

    @ApiModelProperty("父回复ID，用于回复的回复")
    @TableField(value = "parent_id")
    private Long parentId;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("回复内容")
    @TableField(value = "content")
    private String content;

    @ApiModelProperty("状态：pending-待审核,approved-已审核,rejected-已拒绝,hidden-已隐藏")
    @TableField(value = "status")
    private String status;

    @ApiModelProperty("是否匿名")
    @TableField(value = "is_anonymous")
    private Boolean anonymous;

    @ApiModelProperty("扩展字段")
    @TableField(value = "metadata")
    private String metadata;

}
