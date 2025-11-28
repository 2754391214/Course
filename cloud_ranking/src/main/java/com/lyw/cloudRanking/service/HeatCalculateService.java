package com.lyw.cloudRanking.service;

import com.lyw.commonUtil.message.HeatEventMessage;

import java.util.Map;

public interface HeatCalculateService {


    /**
     * 计算事件热度增量
     */
    double calculateHeatIncrement(HeatEventMessage event);

    /**
     * 更新课程热度
     */
    void updateCourseHeat(Long courseId, double increment);

}