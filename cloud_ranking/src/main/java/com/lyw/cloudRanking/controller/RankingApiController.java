package com.lyw.cloudRanking.controller;

import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 排行榜查询")
@RestController
@RequestMapping("/ranking")
public class RankingApiController {

    private final RankingQueryService rankingQueryService;

    @ApiOperation("获取实时排行榜")
    @GetMapping
    public CourseResponseWrapper getRanking(
            @PathVariable String rankingCode,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return rankingQueryService.getRealTimeRanking(rankingCode, page, size);
    }

    @ApiOperation("获取课程排行榜详情")
    @GetMapping("/{rankingCode}/course/{courseId}")
    public CourseResponseWrapper getCourseRankingDetail(
            @PathVariable String rankingCode,
            @PathVariable Long courseId) {
        return rankingQueryService.getCourseRankingDetail(rankingCode, courseId);
    }

    @ApiOperation("获取课程热度趋势")
    @GetMapping("/{rankingCode}/course/{courseId}/trend")
    public CourseResponseWrapper getCourseHeatTrend(
            @PathVariable String rankingCode,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "7d") String period) {
        return rankingQueryService.getCourseHeatTrend(rankingCode, courseId, period);
    }

    @ApiOperation("搜索排行榜课程")
    @GetMapping("/{rankingCode}/search")
    public CourseResponseWrapper searchRanking(
            @PathVariable String rankingCode,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return rankingQueryService.searchRanking(rankingCode, keyword, page, size);
    }
}