package com.lyw.cloudRecommend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 推荐系统配置
 */
@Configuration
@EnableScheduling
public class RecommendConfig {

    /**
     * 定时批量生成推荐结果
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void scheduledBatchRecommendation() {
        // 调用批量推荐服务
    }

    /**
     * 定时清理过期推荐结果
     */
    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    public void scheduledCleanExpiredResults() {
        // 清理过期推荐结果
    }
}