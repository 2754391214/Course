package com.lyw.cloudChoose.dto;

import com.lyw.commonUtil.dto.BaseDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 选课优先级规则表
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Data
public class EnrollmentPrioritiesDto extends BaseDto {

@ApiModelProperty("主键ID")
private Long id;
    
@ApiModelProperty("策略ID")
private Long strategyId;
    
@ApiModelProperty("优先级因素：MAJOR_RELEVANCE-专业相关度, ACADEMIC_STANDING-学业排名, CREDIT_REQUIREMENT-学分需求")
private String priorityFactor;
    
@ApiModelProperty("权重(0-1)，在整体优先级计算中的比重")
private BigDecimal weight;
    
@ApiModelProperty("计算规则配置")
private String calculationRule;
                    
}
