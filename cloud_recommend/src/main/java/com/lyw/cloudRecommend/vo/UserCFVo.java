package com.lyw.cloudRecommend.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户课程操作分数表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@TableName("user_cf")
public class UserCFVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("课程ID")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("分数")
    @TableField(value = "score")
    private int score;

}
