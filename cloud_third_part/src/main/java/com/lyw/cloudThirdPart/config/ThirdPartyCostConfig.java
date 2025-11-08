package com.lyw.cloudThirdPart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "third-party.cost")
public class ThirdPartyCostConfig {

    /**
     * 各服务默认成本配置（元/次）
     */
    private Map<String, Double> defaultCosts = new HashMap<>();

    /**
     * 服务特定配置
     */
    private Map<String, ServiceConfig> services = new HashMap<>();

    /**
     * 每日预算限制（元）
     */
    private double dailyBudget = 1000.0;

    /**
     * 告警阈值比例（0-1）
     */
    private double alertThreshold = 0.8;

    @Data
    public static class ServiceConfig {
        private double unitCost = 0.1;
        private int defaultLimit = 100;
        private long defaultWindow = 60000;
        private CostLevel costLevel = CostLevel.MEDIUM;
    }

    public enum CostLevel {
        LOW(0.01, 1000),     // 低成本，宽松限制
        MEDIUM(0.1, 100),    // 中等成本，适中限制
        HIGH(1.0, 10),       // 高成本，严格限制
        CRITICAL(10.0, 1);   // 关键成本，极严限制

        private final double typicalCost;
        private final int typicalLimit;

        CostLevel(double typicalCost, int typicalLimit) {
            this.typicalCost = typicalCost;
            this.typicalLimit = typicalLimit;
        }

        public double getTypicalCost() { return typicalCost; }
        public int getTypicalLimit() { return typicalLimit; }
    }

    {
        // 初始化默认成本
        defaultCosts.put("SMS", 0.05);
        defaultCosts.put("EMAIL", 0.01);
        defaultCosts.put("OSS_UPLOAD", 0.001);
        defaultCosts.put("OSS_DOWNLOAD", 0.0005);
        defaultCosts.put("AI_CHAT", 0.1);
        defaultCosts.put("AI_IMAGE", 0.5);
    }
}