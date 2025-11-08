package com.lyw.cloudRanking.controller;

import com.lyw.cloudRanking.dto.CourseRankingDetailDto;
import com.lyw.cloudRanking.dto.HeatTrendDto;
import com.lyw.cloudRanking.dto.RankingItemDto;
import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 排行榜查询")
@RestController
@RequestMapping("/api/ranking")
public class RankingApiController {

    private final RankingQueryService rankingQueryService;

    @ApiOperation("获取实时排行榜")
    @GetMapping("/{rankingCode}")
    public CourseResponseWrapper getRanking(
            @PathVariable String rankingCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            List<RankingItemDto> ranking = rankingQueryService.getRealTimeRanking(rankingCode, page, size);
            return CourseResponseWrapper.getSuccess(ranking);

        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("查询排行榜失败");
        }
    }

    @ApiOperation("获取课程排行榜详情")
    @GetMapping("/{rankingCode}/course/{courseId}")
    public CourseResponseWrapper getCourseRankingDetail(
            @PathVariable String rankingCode,
            @PathVariable Long courseId) {

        try {
            CourseRankingDetailDto detail =
                    rankingQueryService.getCourseRankingDetail(rankingCode, courseId);

            if (detail == null) {
                return CourseResponseWrapper.getFailed("课程不在排行榜中");
            }

            return CourseResponseWrapper.getSuccess(detail);

        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("查询课程排行榜详情失败");
        }
    }

    @ApiOperation("获取课程热度趋势")
    @GetMapping("/{rankingCode}/course/{courseId}/trend")
    public CourseResponseWrapper getCourseHeatTrend(
            @PathVariable String rankingCode,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "7d") String period) {

        try {
            HeatTrendDto trend =
                    rankingQueryService.getCourseHeatTrend(rankingCode, courseId, period);
            return CourseResponseWrapper.getSuccess(trend);

        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("查询热度趋势失败");
        }
    }

    @ApiOperation("搜索排行榜课程")
    @GetMapping("/{rankingCode}/search")
    public CourseResponseWrapper searchRanking(
            @PathVariable String rankingCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        try {
            List<RankingItemDto> results =
                    rankingQueryService.searchRanking(rankingCode, keyword, page, size);
            return CourseResponseWrapper.getSuccess(results);

        } catch (Exception e) {
            return CourseResponseWrapper.getFailed("搜索排行榜失败");
        }
    }
}