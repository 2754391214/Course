package com.lyw.cloudRanking.dto;

import com.lyw.cloudRanking.service.impl.RankingQueryServiceImpl;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
@Data
@Accessors(chain = true)
@Builder
public class HeatTrendDto {
    private Long courseId;
    private String period;
    private List<HeatDataPointDto> dataPoints;
}
