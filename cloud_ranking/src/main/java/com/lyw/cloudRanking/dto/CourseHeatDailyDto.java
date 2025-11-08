package com.lyw.cloudRanking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyw.commonUtil.dto.BaseDto;
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
@Accessors(chain = true)
public class CourseHeatDailyDto extends BaseDto {

    private Long id;
    
    @ApiModelProperty("排行榜编码")
    private String rankingCode;
    
    @ApiModelProperty("课程ID")
    private Long courseId;
    
    @ApiModelProperty("统计日期")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "GMT+8")
    private Date heatDate;
    
    @ApiModelProperty("总热度")
    private BigDecimal totalHeat;
    
    @ApiModelProperty("选课热度")
    private BigDecimal enrollmentHeat;
    
    @ApiModelProperty("评分热度")
    private BigDecimal ratingHeat;
    
    @ApiModelProperty("互动热度")
    private BigDecimal interactionHeat;
    
    @ApiModelProperty("当日排名")
    private Integer dailyRank;
    
    @ApiModelProperty("前一日排名")
    private Integer previousRank;
    
}
