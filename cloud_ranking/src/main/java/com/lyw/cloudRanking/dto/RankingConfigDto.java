package com.lyw.cloudRanking.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * <p>
 * 排行榜配置表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
public class RankingConfigDto extends BaseDto {

    @ApiModelProperty("主键ID")
    private Long id;
    
    @ApiModelProperty("排行榜编码")
    private String code;
    
    @ApiModelProperty("排行榜名称")
    private String name;
    
    @ApiModelProperty("计算算法")
    private String algorithm;
    
    @ApiModelProperty("权重配置")
    private String weights;
    
    @ApiModelProperty("状态")
    private Integer status;
    
}
