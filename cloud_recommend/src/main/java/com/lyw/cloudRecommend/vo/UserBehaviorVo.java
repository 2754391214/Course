package com.lyw.cloudRecommend.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户行为表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
@TableName("user_behavior")
public class UserBehaviorVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("课程ID")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("行为类型: VIEW, ENROLL, COMPLETE, LIKE, RATE, SEARCH")
    @TableField(value = "behavior_type")
    private String behaviorType;

    @ApiModelProperty("行为权重")
    @TableField(value = "behavior_weight")
    private BigDecimal behaviorWeight;

    @ApiModelProperty("行为上下文")
    @TableField(value = "context")
    private String context;

    @ApiModelProperty("行为时间")
    @TableField(value = "behavior_time")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date behaviorTime;

}
