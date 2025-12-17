package com.lyw.cloudRanking.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lyw.cloudRanking.service.HeatCalculateService;
import com.lyw.cloudRanking.service.RankingConfigBo;
import com.lyw.cloudRanking.vo.RankingConfigVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.message.HeatEventMessage;
import com.lyw.commonUtil.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class HeatCalculateServiceImpl implements HeatCalculateService {
    @Resource
    private RankingConfigBo rankingConfigBo;
    @Resource
    private RedisUtils redisUtils;

    // 默认权重配置
    private static final Map<String, Double> DEFAULT_WEIGHTS = new HashMap<>();
    static {
        DEFAULT_WEIGHTS.put(CommonKeyConstant.ENROLLMENT, 5.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.WITHDRAWAL, 3.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.RATING, 8.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.LIKE, 2.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.FAVORITE, 3.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.COMMENT, 4.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.REVIEW, 1.0);
        DEFAULT_WEIGHTS.put(CommonKeyConstant.REPLY, 1.0);
    }

    /**
     * 计算事件热度增量
     */
    public double calculateHeatIncrement(HeatEventMessage event) {
        // 获取排行榜配置
        RankingConfigVo config = rankingConfigBo.getConfigByCode("course_heat");
        Map<String, Double> weights = getWeightsFromConfig(config);

        // 并行执行三个独立计算
        CompletableFuture<Double> baseIncrementFuture = CompletableFuture
                .supplyAsync(() -> getBaseIncrement(event, weights));

        CompletableFuture<Double> qualityFactorFuture = CompletableFuture
                .supplyAsync(() -> calculateQualityFactor(event));

        CompletableFuture<Double> timeFactorFuture = CompletableFuture
                .supplyAsync(() -> calculateTimeFactor(event));

        // 等待所有任务完成并计算结果
        try {
            return CompletableFuture.allOf(baseIncrementFuture, qualityFactorFuture, timeFactorFuture)
                    .thenApply(v -> baseIncrementFuture.join() * qualityFactorFuture.join() * timeFactorFuture.join())
                    .get(); // 获取最终结果
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            // 降级为串行计算
            return getBaseIncrement(event, weights) * calculateQualityFactor(event) * calculateTimeFactor(event);
        }
    }

    /**
     * 获取权重配置
     */
    private Map<String, Double> getWeightsFromConfig(RankingConfigVo config) {
        if (ObjectUtil.isEmpty(config) || StrUtil.isEmpty(config.getWeights())) {
            return DEFAULT_WEIGHTS;
        }
        try {
            return JSON.parseObject(config.getWeights(), new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            log.warn("解析权重配置失败，使用默认配置", e);
            return DEFAULT_WEIGHTS;
        }
    }

    /**
     * 计算基础增量
     */
    private double getBaseIncrement(HeatEventMessage event, Map<String, Double> weights) {
        double base = weights.getOrDefault(event.getEventType(), 1.0);

        // 评分事件特殊处理
        if (CommonKeyConstant.RATING.equals(event.getEventType()) && event.getRatingValue() != null) {
            return base * (event.getRatingValue() / 5.0); // 按评分比例计算
        }

        // 互动事件根据目标类型调整权重
        if (CommonKeyConstant.LIKE.equals(event.getEventType()) || CommonKeyConstant.FAVORITE.equals(event.getEventType())) {
            // 评价的点赞/收藏权重高于回复
            if (CommonKeyConstant.REVIEW.equals(event.getTargetType())) {
                return base * 1.5;
            } else if (CommonKeyConstant.REPLY.equals(event.getTargetType())) {
                return base * 1.0;
            }
        }

        return base;
    }

    /**
     * 计算质量因子
     */
    private double calculateQualityFactor(HeatEventMessage event) {
        double factor = 1.0;

        // 可以根据事件的具体内容调整质量因子
        // 例如：评论长度、评分是否带文字等
        if (CommonKeyConstant.COMMENT.equals(event.getEventType())) {
            // 假设事件数据中包含评论长度
            factor += 0.5; // 有评论额外加分
        }

        return Math.min(factor, 2.0); // 质量因子上限2.0
    }

    /**
     * 计算时间因子（时间衰减）
     */
    private double calculateTimeFactor(HeatEventMessage event) {
        if (ObjectUtil.isEmpty(event.getEventTime())) {
            return 1.0;
        }

        long eventTime = event.getEventTime().getTime();
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - eventTime;

        // 7天半衰期
        long halfLife = 7 * 24 * 60 * 60 * 1000L;
        return Math.pow(0.5, (double) timeDiff / halfLife);
    }

    /**
     * 更新课程热度
     */
    public void updateCourseHeat(Long courseId, double increment) {
        String heatKey = String.format(RedisKeyConstant.COURSE_HEAT_WHO, courseId);

        try {
            // 使用原子操作更新热度值
            redisUtils.incrByFloat(heatKey, increment);

            // 异步更新排行榜
            new Thread(() -> updateRankingAsync(courseId, heatKey, RedisKeyConstant.RANKING_COURSE_HEAT)).start();

            log.debug("更新课程热度: courseId={}, increment={}", courseId, increment);

        } catch (Exception e) {
            log.error("更新课程热度失败: courseId={}", courseId, e);
        }
    }

    /**
     * 异步更新排行榜
     */
    private void updateRankingAsync(Long courseId, String heatKey, String rankingKey) {
        try {
            Double currentHeat = redisUtils.get(heatKey);
            if (ObjectUtil.isNotEmpty(currentHeat)) {
                redisUtils.zAdd(rankingKey, courseId, currentHeat);
                // 限制排行榜大小，避免内存问题
                redisUtils.zRemoveRange(rankingKey, 0, -1001);
            }
        } catch (Exception e) {
            log.error("异步更新排行榜失败: courseId={}", courseId, e);
        }
    }
}