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
 * 推荐配置表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
@TableName("recommend_config")
public class RecommendConfigVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("策略名称")
    @TableField(value = "strategy_name")
    private String strategyName;

    @ApiModelProperty("策略类型: COLLABORATIVE, CONTENT, HYBRID, REAL_TIME")
    @TableField(value = "strategy_type")
    private String strategyType;

    @ApiModelProperty("召回配置")
    @TableField(value = "recall_config")
    private String recallConfig;

    @ApiModelProperty("排序配置")
    @TableField(value = "ranking_config")
    private String rankingConfig;

    @ApiModelProperty("重排配置")
    @TableField(value = "rerank_config")
    private String rerankConfig;

    @ApiModelProperty("策略权重")
    @TableField(value = "weight")
    private BigDecimal weight;

    @ApiModelProperty("状态")
    @TableField(value = "status")
    private Integer status;

    @ApiModelProperty("描述")
    @TableField(value = "description")
    private String description;

}
