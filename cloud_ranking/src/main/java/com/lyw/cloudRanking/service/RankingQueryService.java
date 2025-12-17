package com.lyw.cloudRanking.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface RankingQueryService {

    /**
     * 获取课程实时排行榜
     */
    CourseResponseWrapper getCourseRanking(int page, int size) ;

    /**
     * 获取课程热度趋势
     */
    CourseResponseWrapper getCourseHeatTrend(Long courseId, String period);

}