package com.lyw.cloudRanking.dto;

import lombok.Builder;
import lombok.Data;


import java.util.List;
@Data
@Builder
public class HeatTrendDto {
    private Long courseId;
    private String period;
    private List<HeatDataPointDto> dataPoints;
}
