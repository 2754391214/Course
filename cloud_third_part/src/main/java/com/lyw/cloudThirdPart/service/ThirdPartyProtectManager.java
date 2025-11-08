package com.lyw.cloudThirdPart.service;

import com.lyw.cloudThirdPart.aspect.ThirdPartyProtect;
import com.lyw.cloudThirdPart.config.ThirdPartyCostConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class ThirdPartyProtectManager {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ThirdPartyCostConfig costConfig;

    @Autowired
    private DefaultRedisScript<Boolean> costAwareLimitScript;

    @Autowired
    private DefaultRedisScript<Boolean> behaviorAnalysisScript;

    @Autowired
    private DefaultRedisScript<Boolean> budgetLimitScript;

    // Redis键前缀
    private static final String COST_PREFIX = "third_party:cost:";
    private static final String BEHAVIOR_PREFIX = "third_party:behavior:";
    private static final String BUDGET_PREFIX = "third_party:budget:";

    /**
     * 检查是否允许调用第三方服务
     */
    public boolean allow(ThirdPartyProtect protect, String callerId, Map<String, Object> context) {
        String serviceType = protect.service().name();

        // 1. 成本感知限流检查
        if (!checkCostAwareLimit(protect, callerId, serviceType, context)) {
            log.warn("成本感知限流拦截: service={}, caller={}", serviceType, callerId);
            return false;
        }

        // 2. 行为分析检查
        if (protect.enableContextCheck() && !checkBehaviorAnalysis(protect, callerId, serviceType, context)) {
            log.warn("行为分析拦截: service={}, caller={}", serviceType, callerId);
            return false;
        }

        // 3. 预算检查
        if (!checkDailyBudget(serviceType, getServiceCost(serviceType))) {
            log.warn("日预算超限拦截: service={}", serviceType);
            return false;
        }

        return true;
    }

    /**
     * 成本感知限流检查
     */
    private boolean checkCostAwareLimit(ThirdPartyProtect protect, String callerId,
                                        String serviceType, Map<String, Object> context) {
        String key = String.format("%s%s:%s", COST_PREFIX, serviceType, callerId);

        // 根据成本敏感度动态调整限制
        int actualLimit = adjustLimitBySensitivity(protect.count(), protect.sensitivity());
        long actualWindow = adjustWindowBySensitivity(protect.timeWindow(), protect.sensitivity());

        Boolean result = stringRedisTemplate.execute(
                costAwareLimitScript,
                Collections.singletonList(key),
                String.valueOf(actualLimit),
                String.valueOf(actualWindow),
                String.valueOf(protect.coolDown()),
                String.valueOf(System.currentTimeMillis()),
                getCostLevel(serviceType)
        );

        return result != null && result;
    }

    /**
     * 行为分析检查
     */
    private boolean checkBehaviorAnalysis(ThirdPartyProtect protect, String callerId,
                                          String serviceType, Map<String, Object> context) {
        String key = String.format("%s%s:%s", BEHAVIOR_PREFIX, serviceType, callerId);

        Boolean result = stringRedisTemplate.execute(
                behaviorAnalysisScript,
                Collections.singletonList(key),
                String.valueOf(protect.count()),
                String.valueOf(protect.timeWindow()),
                String.valueOf(protect.coolDown()),
                callerId,
                String.valueOf(System.currentTimeMillis()),
                extractBehaviorFeatures(context)
        );

        return result != null && result;
    }

    /**
     * 日预算检查
     */
    private boolean checkDailyBudget(String serviceType, double cost) {
        String today = LocalDate.now().toString();
        String key = String.format("%s%s:%s", BUDGET_PREFIX, serviceType, today);

        Boolean result = stringRedisTemplate.execute(
                budgetLimitScript,
                Collections.singletonList(key),
                String.valueOf(costConfig.getDailyBudget()),
                String.valueOf(cost),
                String.valueOf(costConfig.getAlertThreshold())
        );

        if (result != null && !result) {
            // 触发预算告警
            triggerBudgetAlert(serviceType, today);
        }

        return result != null && result;
    }

    /**
     * 根据成本敏感度调整限制
     */
    private int adjustLimitBySensitivity(int baseLimit, ThirdPartyProtect.CostSensitivity sensitivity) {
        switch (sensitivity) {
            case LOW: return (int) (baseLimit * 1.5);     // 宽松：增加50%
            case HIGH: return (int) (baseLimit * 0.5);    // 严格：减少50%
            default: return baseLimit;                    // 中等：保持不变
        }
    }

    private long adjustWindowBySensitivity(long baseWindow, ThirdPartyProtect.CostSensitivity sensitivity) {
        switch (sensitivity) {
            case LOW: return (long) (baseWindow * 0.8);   // 宽松：缩短窗口
            case HIGH: return (long) (baseWindow * 1.5);  // 严格：延长窗口
            default: return baseWindow;
        }
    }

    /**
     * 获取服务成本
     */
    private double getServiceCost(String serviceType) {
        return costConfig.getDefaultCosts().getOrDefault(serviceType, 0.1);
    }

    /**
     * 获取成本等级
     */
    private String getCostLevel(String serviceType) {
        double cost = getServiceCost(serviceType);
        if (cost >= 1.0) return "CRITICAL";
        if (cost >= 0.1) return "HIGH";
        if (cost >= 0.01) return "MEDIUM";
        return "LOW";
    }

    /**
     * 提取行为特征
     */
    private String extractBehaviorFeatures(Map<String, Object> context) {
        // 提取调用模式特征
        List<String> features = new ArrayList<>();

        // 时间特征
        features.add("hour:" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

        // 业务特征
        if (context.containsKey("businessType")) {
            features.add("business:" + context.get("businessType"));
        }
        if (context.containsKey("recipientCount")) {
            int count = (Integer) context.get("recipientCount");
            features.add("batch_size:" + (count > 10 ? "large" : "small"));
        }

        return String.join(",", features);
    }

    /**
     * 触发预算告警
     */
    private void triggerBudgetAlert(String serviceType, String date) {
        String alertKey = String.format("%salert:%s:%s", BUDGET_PREFIX, serviceType, date);
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(alertKey))) {
            // 发送告警通知
            log.error("第三方服务预算告警: service={}, date={}, budget={}",
                    serviceType, date, costConfig.getDailyBudget());

            // 标记已告警，避免重复通知
            stringRedisTemplate.opsForValue().set(alertKey, "1", java.time.Duration.ofHours(24));
        }
    }

    /**
     * 记录调用成本
     */
    public void recordCost(String serviceType, String callerId, double cost) {
        String today = LocalDate.now().toString();
        String key = String.format("%s%s:%s", BUDGET_PREFIX, serviceType, today);

        // 累加日成本
        stringRedisTemplate.opsForValue().increment(key, cost);
        // 设置过期时间（2天，防止跨天问题）
        stringRedisTemplate.expire(key, java.time.Duration.ofHours(48));

        log.debug("记录服务调用成本: service={}, caller={}, cost={}", serviceType, callerId, cost);
    }

    /**
     * 获取今日累计成本
     */
    public double getTodayCost(String serviceType) {
        String today = LocalDate.now().toString();
        String key = String.format("%s%s:%s", BUDGET_PREFIX, serviceType, today);

        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? Double.parseDouble(value) : 0.0;
    }

    /**
     * 获取服务调用统计
     */
    public Map<String, Object> getServiceStats(String serviceType, String callerId) {
        Map<String, Object> stats = new HashMap<>();

        // 今日成本
        stats.put("todayCost", getTodayCost(serviceType));

        // 调用频率
        String costKey = String.format("%s%s:%s", COST_PREFIX, serviceType, callerId);
        String count = stringRedisTemplate.opsForValue().get(costKey);
        stats.put("callCount", count != null ? count : "0");

        // 预算使用率
        double usageRate = getTodayCost(serviceType) / costConfig.getDailyBudget();
        stats.put("budgetUsageRate", String.format("%.2f%%", usageRate * 100));

        return stats;
    }
}