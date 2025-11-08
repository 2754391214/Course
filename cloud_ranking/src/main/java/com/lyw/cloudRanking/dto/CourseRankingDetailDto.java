package com.lyw.cloudRanking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class CourseRankingDetailDto {
    private Long courseId;
    private String rankingCode;
    private Double heatScore;
    private Integer currentRank;
    private CourseBasicInfoDto courseInfo;
}
