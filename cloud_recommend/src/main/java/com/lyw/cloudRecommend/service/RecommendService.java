package com.lyw.cloudRecommend.service;

import com.lyw.cloudRecommend.dto.UserBehaviorMessage;
import com.lyw.cloudRecommend.vo.RecommendConfigVo;
import com.lyw.cloudRecommend.vo.RecommendResultsVo;
import com.lyw.cloudRecommend.vo.UserBehaviorVo;
import com.lyw.cloudRecommend.vo.UserProfileVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.util.List;
import java.util.Map;

/**
 * 推荐服务接口
 */
public interface RecommendService {

    /**
     * 获取个性化推荐
     */
    CourseResponseWrapper<List<Long>> getPersonalizedRecommendation(Long userId, String recommendType, int size);

    /**
     * 实时推荐（基于最近行为）
     */
    CourseResponseWrapper<List<Long>> getRealTimeRecommendation(Long userId, int size);

    /**
     * 处理用户行为
     */
    CourseResponseWrapper<Boolean> processUserBehavior(UserBehaviorVo userBehavior);

    /**
     * 更新用户画像
     */
    CourseResponseWrapper<Boolean> updateUserProfile(Long userId);

    /**
     * 获取推荐配置
     */
    CourseResponseWrapper<List<RecommendConfigVo>> getActiveStrategies();

    /**
     * 批量生成推荐结果
     */
    CourseResponseWrapper<Boolean> batchGenerateRecommendations();

    void handleUserBehaviorMessage(UserBehaviorMessage message);

    void handleFailedUserBehaviorMessage(UserBehaviorMessage message);
}