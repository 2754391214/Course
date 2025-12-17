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

    @ApiOperation("获取课程实时排行榜")
    @GetMapping("/course")
    public CourseResponseWrapper getCourseRanking(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size) {
        return rankingQueryService.getCourseRanking(page, size);
    }

    @ApiOperation("获取课程热度趋势")
    @GetMapping("/course/{courseId}/trend")
    public CourseResponseWrapper getCourseHeatTrend(
            @PathVariable Long courseId,
            @RequestParam(value = "period",defaultValue = "7d") String period) {
        return rankingQueryService.getCourseHeatTrend(courseId, period);
    }

}