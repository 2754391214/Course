package com.lyw.cloudRanking.service;

import com.lyw.cloudRanking.dto.CourseRankingDetailDto;
import com.lyw.cloudRanking.dto.HeatTrendDto;
import com.lyw.cloudRanking.dto.RankingItemDto;

import java.util.List;

public interface RankingQueryService {

    /**
     * 获取实时排行榜
     */
    List<RankingItemDto> getRealTimeRanking(String rankingCode, int page, int size) ;

    /**
     * 获取课程排行榜详情
     */
    CourseRankingDetailDto getCourseRankingDetail(String rankingCode, Long courseId);

    /**
     * 获取课程热度趋势
     */
    HeatTrendDto getCourseHeatTrend(String rankingCode, Long courseId, String period);

    /**
     * 搜索排行榜课程
     */
    List<RankingItemDto> searchRanking(String rankingCode, String keyword, int page, int size);

    /**
     * 刷新排行榜缓存
     */
    void refreshRankingCache(String rankingCode);

}