package com.lyw.cloudRanking.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.vo.BaseVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 课程热度日快照表
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Data
@TableName("course_heat_daily")
public class CourseHeatDailyVo extends BaseVo {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("排行榜编码")
    @TableField(value = "ranking_code")
    private String rankingCode;

    @ApiModelProperty("课程ID")
    @TableField(value = "course_id")
    private Long courseId;

    @ApiModelProperty("统计日期")
    @TableField(value = "heat_date")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date heatDate;

    @ApiModelProperty("总热度")
    @TableField(value = "total_heat")
    private BigDecimal totalHeat;

    @ApiModelProperty("选课热度")
    @TableField(value = "enrollment_heat")
    private BigDecimal enrollmentHeat;

    @ApiModelProperty("评分热度")
    @TableField(value = "rating_heat")
    private BigDecimal ratingHeat;

    @ApiModelProperty("互动热度")
    @TableField(value = "interaction_heat")
    private BigDecimal interactionHeat;

    @ApiModelProperty("当日排名")
    @TableField(value = "daily_rank")
    private Integer dailyRank;

    @ApiModelProperty("前一日排名")
    @TableField(value = "previous_rank")
    private Integer previousRank;

}
