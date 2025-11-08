package com.lyw.cloudRanking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Builder
public class HeatDataPointDto {
    private Date date;
    private Double heatValue;
    private Integer rank;

}
