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
 * 推荐结果表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
@TableName("recommend_results")
public class RecommendResultsVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户ID")
    @TableField(value = "user_id")
    private Long userId;

    @ApiModelProperty("推荐类型: HOME_PAGE, COURSE_DETAIL, PERSONALIZED")
    @TableField(value = "recommend_type")
    private String recommendType;

    @ApiModelProperty("推荐课程ID列表")
    @TableField(value = "course_ids")
    private String courseIds;

    @ApiModelProperty("推荐分数")
    @TableField(value = "scores")
    private String scores;

    @ApiModelProperty("使用的策略")
    @TableField(value = "strategy_used")
    private String strategyUsed;

    @ApiModelProperty("过期时间")
    @TableField(value = "expire_time")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

}
