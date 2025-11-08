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
 * 互动表
 * </p>
 *
 * @author lyw
 * @since 2025/10/29
 */
@Data
@Accessors(chain = true)
@TableName("interactions")
public class InteractionsVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID，关联用户表")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("互动目标类型：course-课程,review-评价,teacher-教师")
    @TableField(value = "target_type")
    private String targetType;

    @ApiModelProperty("目标ID，根据target_type对应不同表的ID")
    @TableField(value = "target_id")
    private Long targetId;

    @ApiModelProperty("互动类型：like-点赞,favorite-收藏,useful-有用,share-分享,follow-关注,report-举报")
    @TableField(value = "interaction_type")
    private String interactionType;

    @ApiModelProperty("扩展字段，例如收藏夹名称等")
    @TableField(value = "metadata")
    private String metadata;


    @ApiModelProperty("点赞总数")
    @TableField(exist = false)
    private Long likeCount;


    @ApiModelProperty("有用总数")
    @TableField(exist = false)
    private Long usefulCount;
}
