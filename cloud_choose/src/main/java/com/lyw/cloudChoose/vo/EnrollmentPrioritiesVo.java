package com.lyw.cloudChoose.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lyw.commonUtil.vo.BaseVo;
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
@TableName("enrollment_priorities")
public class EnrollmentPrioritiesVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("策略ID")
    @TableField(value = "strategy_id")
    private Long strategyId;

    @ApiModelProperty("优先级因素：MAJOR_RELEVANCE-专业相关度, ACADEMIC_STANDING-学业排名, CREDIT_REQUIREMENT-学分需求")
    @TableField(value = "priority_factor")
    private String priorityFactor;

    @ApiModelProperty("权重(0-1)，在整体优先级计算中的比重")
    @TableField(value = "weight")
    private BigDecimal weight;

    @ApiModelProperty("计算规则配置")
    @TableField(value = "calculation_rule")
    private String calculationRule;

}
