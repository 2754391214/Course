package com.lyw.cloudRanking.dto;

import com.lyw.cloudRanking.service.impl.RankingQueryServiceImpl;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class RankingItemDto {
    private Long courseId;
    private Double heatScore;
    private Integer rank;
    private CourseBasicInfoDto courseInfo;
}
