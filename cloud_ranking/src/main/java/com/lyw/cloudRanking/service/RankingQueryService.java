package com.lyw.cloudRanking.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface RankingQueryService {

    /**
     * 获取实时排行榜
     */
    CourseResponseWrapper getRealTimeRanking(String rankingCode, int page, int size) ;

    /**
     * 获取课程排行榜详情
     */
    CourseResponseWrapper getCourseRankingDetail(String rankingCode, Long courseId);

    /**
     * 获取课程热度趋势
     */
    CourseResponseWrapper getCourseHeatTrend(String rankingCode, Long courseId, String period);

    /**
     * 搜索排行榜课程
     */
    CourseResponseWrapper searchRanking(String rankingCode, String keyword, int page, int size);

}