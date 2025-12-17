package com.lyw.cloudRecommend.controller;

import com.lyw.cloudRecommend.service.RecommendService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 推荐控制器
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@RestController
@RequestMapping("/recommend")
@Api(tags = "推荐服务")
public class RecommendController {

    private final RecommendService service;

    @GetMapping("/{userId}")
    @ApiOperation("获取推荐列表")
    public CourseResponseWrapper getRecommendationList(@PathVariable Long userId, @RequestParam(defaultValue = "20") int size) {
        return service.getRecommendationList(userId, size);
    }
}