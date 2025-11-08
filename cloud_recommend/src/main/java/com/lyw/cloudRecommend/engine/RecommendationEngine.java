package com.lyw.cloudRecommend.engine;

import com.lyw.cloudRecommend.strategy.RecommendStrategy;
import com.lyw.cloudRecommend.vo.RecommendConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐引擎
 */
@Slf4j
@Component
public class RecommendationEngine {

    private final Map<String, RecommendStrategy> strategyMap;

    public RecommendationEngine(List<RecommendStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(RecommendStrategy::getStrategyType, strategy -> strategy));
    }

    /**
     * 执行推荐流程
     */
    public Map<String, Object> executeRecommendation(Long userId, String recommendType,
                                                     List<RecommendConfigVo> strategies, int size) {
        Map<String, Object> result = new HashMap<>();
        List<Long> finalRecommendations = new ArrayList<>();
        Map<Long, Double> finalScores = new HashMap<>();
        List<String> usedStrategies = new ArrayList<>();

        try {
            // 1. 多策略召回
            Map<String, List<Long>> recallResults = recallPhase(userId, strategies, size * 3);

            // 2. 多策略融合排序
            Map<Long, Double> rankingScores = rankingPhase(userId, recallResults, strategies);

            // 3. 重排阶段
            finalRecommendations = rerankPhase(rankingScores, size);
            finalScores = rankingScores;
            usedStrategies = strategies.stream()
                    .map(RecommendConfigVo::getStrategyName)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("推荐引擎执行失败, userId: {}", userId, e);
            // 返回热门推荐作为降级方案
            finalRecommendations = getPopularRecommendations(size);
        }

        result.put("recommendations", finalRecommendations);
        result.put("scores", finalScores);
        result.put("strategies", usedStrategies);

        return result;
    }

    /**
     * 召回阶段 - 多策略并行召回
     */
    private Map<String, List<Long>> recallPhase(Long userId, List<RecommendConfigVo> strategies, int recallSize) {
        Map<String, List<Long>> recallResults = new HashMap<>();

        strategies.forEach(strategy -> {
            RecommendStrategy recommendStrategy = strategyMap.get(strategy.getStrategyType());
            if (recommendStrategy != null) {
                try {
                    Map<String, Object> config = parseConfig(strategy.getRecallConfig());
                    List<Long> items = recommendStrategy.recommend(userId, recallSize, config);
                    recallResults.put(strategy.getStrategyName(), items);
                } catch (Exception e) {
                    log.error("策略召回失败: {}", strategy.getStrategyName(), e);
                }
            }
        });

        return recallResults;
    }

    /**
     * 排序阶段 - 多策略分数融合
     */
    private Map<Long, Double> rankingPhase(Long userId, Map<String, List<Long>> recallResults,
                                           List<RecommendConfigVo> strategies) {
        Map<Long, Double> finalScores = new HashMap<>();
        Set<Long> allCandidates = recallResults.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());

        // 为每个策略计算分数并加权融合
        strategies.forEach(strategy -> {
            List<Long> candidates = recallResults.get(strategy.getStrategyName());
            if (candidates != null && !candidates.isEmpty()) {
                RecommendStrategy recommendStrategy = strategyMap.get(strategy.getStrategyType());
                Map<String, Object> config = parseConfig(strategy.getRankingConfig());
                Map<Long, Double> strategyScores = recommendStrategy.calculateScores(userId, candidates, config);

                // 加权融合
                strategyScores.forEach((itemId, score) -> {
                    double weightedScore = score * strategy.getWeight().doubleValue();
                    finalScores.merge(itemId, weightedScore, Double::sum);
                });
            }
        });

        return finalScores;
    }

    /**
     * 重排阶段
     */
    private List<Long> rerankPhase(Map<Long, Double> rankingScores, int size) {
        return rankingScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(size)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 热门推荐降级方案
     */
    private List<Long> getPopularRecommendations(int size) {
        // 实现热门课程推荐逻辑
        return new ArrayList<>();
    }

    private Map<String, Object> parseConfig(String configJson) {
        // 解析JSON配置
        try {
            // 使用Jackson或Gson解析配置
            return new HashMap<>();
        } catch (Exception e) {
            log.error("配置解析失败: {}", configJson, e);
            return new HashMap<>();
        }
    }
}