package com.lyw.cloudRecommend.service;

import com.lyw.commonUtil.message.UserBehaviorMessage;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * 推荐服务接口
 */
public interface RecommendService {

    /**
     * 获取推荐列表
     */
    CourseResponseWrapper getRecommendationList(Long userId, int size);
    /**
     * 处理用户行为
     */
    void handleUserBehaviorMessage(UserBehaviorMessage message);
    /**
     * 处理失败的消息
     */
    void handleFailedUserBehaviorMessage(UserBehaviorMessage message);
}