package com.lyw.cloudRanking.dto;

import lombok.Builder;
import lombok.Data;


import java.util.Date;

@Data
@Builder
public class HeatDataPointDto {
    private Date date;
    private Double heatValue;
    private Integer rank;

}
