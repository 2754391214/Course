package com.lyw.cloudRanking.dto;

import com.lyw.commonUtil.dto.CourseBasicInfoDto;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class CourseRankingDetailDto {
    private Long courseId;
    private Double heatScore;
    private Integer currentRank;
    private CourseBasicInfoDto courseInfo;
}
