package com.lyw.cloudRanking.vo;

import com.lyw.commonUtil.vo.BaseVo;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.Accessors;

/**
 * <p>
 * 排行榜配置表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@Accessors(chain = true)
@TableName("ranking_config")
public class RankingConfigVo extends BaseVo {

    @ApiModelProperty("主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("排行榜编码")
    @TableField(value = "code")
    private String code;

    @ApiModelProperty("排行榜名称")
    @TableField(value = "name")
    private String name;

    @ApiModelProperty("计算算法")
    @TableField(value = "algorithm")
    private String algorithm;

    @ApiModelProperty("权重配置")
    @TableField(value = "weights")
    private String weights;

    @ApiModelProperty("状态")
    @TableField(value = "status")
    private Integer status;

}
