package com.lyw.cloudRanking.dto;

import com.lyw.commonUtil.dto.CourseBasicInfoDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class RankingItemDto {
    @ApiModelProperty("课程ID")
    private Long courseId;
    @ApiModelProperty("热度分数")
    private Double heatScore;
    @ApiModelProperty("排行")
    private Integer rank;
    @ApiModelProperty("课程基本信息")
    private CourseBasicInfoDto courseInfo;
}
