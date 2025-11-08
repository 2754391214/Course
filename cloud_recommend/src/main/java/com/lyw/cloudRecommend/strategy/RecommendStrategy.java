package com.lyw.cloudRecommend.strategy;

import java.util.List;
import java.util.Map;

/**
 * 推荐策略接口
 */
public interface RecommendStrategy {

    /**
     * 策略类型
     */
    String getStrategyType();

    /**
     * 生成推荐
     */
    List<Long> recommend(Long userId, int size, Map<String, Object> config);

    /**
     * 计算推荐分数
     */
    Map<Long, Double> calculateScores(Long userId, List<Long> candidateItems, Map<String, Object> config);
}