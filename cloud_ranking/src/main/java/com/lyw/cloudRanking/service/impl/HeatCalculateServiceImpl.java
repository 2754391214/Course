package com.lyw.cloudRanking.service.impl;

import com.lyw.cloudRanking.dto.HeatEventMessage;
import com.lyw.cloudRanking.service.HeatCalculateService;
import com.lyw.cloudRanking.service.RankingConfigBo;
import com.lyw.cloudRanking.vo.RankingConfigVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class HeatCalculateServiceImpl implements HeatCalculateService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RankingConfigBo rankingConfigBo;

    // 默认权重配置
    private static final Map<String, Double> DEFAULT_WEIGHTS = new HashMap<>();
    static {
        DEFAULT_WEIGHTS.put("ENROLLMENT", 5.0);
        DEFAULT_WEIGHTS.put("WITHDRAWAL", -3.0);
        DEFAULT_WEIGHTS.put("RATING", 8.0);
        DEFAULT_WEIGHTS.put("LIKE", 2.0);
        DEFAULT_WEIGHTS.put("FAVORITE", 3.0);
        DEFAULT_WEIGHTS.put("COMMENT", 4.0);
    }

    /**
     * 计算事件热度增量
     */
    public double calculateHeatIncrement(HeatEventMessage event) {
        // 获取排行榜配置
        RankingConfigVo config = rankingConfigBo.getConfigByCode("course_heat");
        Map<String, Double> weights = getWeightsFromConfig(config);

        double baseIncrement = getBaseIncrement(event, weights);
        double qualityFactor = calculateQualityFactor(event);
        double timeFactor = calculateTimeFactor(event);

        return baseIncrement * qualityFactor * timeFactor;
    }

    /**
     * 获取权重配置
     */
    private Map<String, Double> getWeightsFromConfig(RankingConfigVo config) {
        if (config == null || config.getWeights() == null) {
            return DEFAULT_WEIGHTS;
        }

        try {
            // 简单的JSON解析，实际可以使用Jackson
            // 这里简化处理，实际项目中需要根据weights字段的实际格式解析
            return DEFAULT_WEIGHTS;
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

        // 处理取消操作（REMOVE action）
        if ("REMOVE".equals(event.getAction())) {
            return -base;
        }

        // 评分事件特殊处理
        if ("RATING".equals(event.getEventType()) && event.getRatingValue() != null) {
            return base * (event.getRatingValue() / 5.0); // 按评分比例计算
        }

        // 互动事件根据目标类型调整权重
        if ("LIKE".equals(event.getEventType()) || "FAVORITE".equals(event.getEventType())) {
            // 评价的点赞/收藏权重高于回复
            if ("review".equals(event.getTargetType())) {
                return base * 1.5;
            } else if ("reply".equals(event.getTargetType())) {
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
        if ("COMMENT".equals(event.getEventType())) {
            // 假设事件数据中包含评论长度
            factor += 0.5; // 有评论额外加分
        }

        return Math.min(factor, 2.0); // 质量因子上限2.0
    }

    /**
     * 计算时间因子（时间衰减）
     */
    private double calculateTimeFactor(HeatEventMessage event) {
        if (event.getEventTime() == null) {
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
        String heatKey = String.format("course:heat:%d", courseId);
        String rankingKey = "ranking:course:heat";

        try {
            // 使用原子操作更新热度值
            redisTemplate.opsForValue().increment(heatKey, increment);

            // 异步更新排行榜
            new Thread(() -> updateRankingAsync(courseId, heatKey, rankingKey)).start();

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
            Double currentHeat = (Double) redisTemplate.opsForValue().get(heatKey);
            if (currentHeat != null) {
                redisTemplate.opsForZSet().add(rankingKey, courseId, currentHeat);

                // 限制排行榜大小，避免内存问题
                redisTemplate.opsForZSet().removeRange(rankingKey, 0, -1001);
            }
        } catch (Exception e) {
            log.error("异步更新排行榜失败: courseId={}", courseId, e);
        }
    }

    /**
     * 批量更新热度
     */
    public void batchUpdateHeat(Map<Long, Double> heatUpdates) {
        heatUpdates.forEach(this::updateCourseHeat);
    }
}