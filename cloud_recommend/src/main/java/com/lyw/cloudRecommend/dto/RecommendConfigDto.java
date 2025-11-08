package com.lyw.cloudRecommend.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

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
public class RecommendConfigDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("策略名称")
    private String strategyName;
    
    @ApiModelProperty("策略类型: COLLABORATIVE, CONTENT, HYBRID, REAL_TIME")
    private String strategyType;
    
    @ApiModelProperty("召回配置")
    private String recallConfig;
    
    @ApiModelProperty("排序配置")
    private String rankingConfig;
    
    @ApiModelProperty("重排配置")
    private String rerankConfig;
    
    @ApiModelProperty("策略权重")
    private BigDecimal weight;
    
    @ApiModelProperty("状态")
    private Integer status;
    
    @ApiModelProperty("描述")
    private String description;
                    
}
