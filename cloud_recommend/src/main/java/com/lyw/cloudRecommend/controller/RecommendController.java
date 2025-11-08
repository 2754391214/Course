package com.lyw.cloudRecommend.controller;

import com.lyw.cloudRecommend.service.RecommendService;
import com.lyw.cloudRecommend.vo.UserBehaviorVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/recommend")
@Api(tags = "推荐服务")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @GetMapping("/personalized/{userId}")
    @ApiOperation("获取个性化推荐")
    public CourseResponseWrapper<List<Long>> getPersonalizedRecommendation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "PERSONALIZED") String recommendType,
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取个性化推荐, userId: {}, type: {}, size: {}", userId, recommendType, size);
        return recommendService.getPersonalizedRecommendation(userId, recommendType, size);
    }

    @GetMapping("/realtime/{userId}")
    @ApiOperation("获取实时推荐")
    public CourseResponseWrapper<List<Long>> getRealTimeRecommendation(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int size) {

        log.info("获取实时推荐, userId: {}, size: {}", userId, size);
        return recommendService.getRealTimeRecommendation(userId, size);
    }

    @PostMapping("/behavior")
    @ApiOperation("记录用户行为")
    public CourseResponseWrapper<Boolean> recordUserBehavior(@RequestBody UserBehaviorVo userBehavior) {
        log.info("记录用户行为, userId: {}, courseId: {}, behavior: {}",
                userBehavior.getUserId(), userBehavior.getCourseId(), userBehavior.getBehaviorType());
        return recommendService.processUserBehavior(userBehavior);
    }

    @PostMapping("/profile/refresh/{userId}")
    @ApiOperation("刷新用户画像")
    public CourseResponseWrapper<Boolean> refreshUserProfile(@PathVariable Long userId) {
        log.info("刷新用户画像, userId: {}", userId);
        return recommendService.updateUserProfile(userId);
    }

    @PostMapping("/batch/generate")
    @ApiOperation("批量生成推荐")
    public CourseResponseWrapper<Boolean> batchGenerateRecommendations() {
        log.info("批量生成推荐");
        return recommendService.batchGenerateRecommendations();
    }
}