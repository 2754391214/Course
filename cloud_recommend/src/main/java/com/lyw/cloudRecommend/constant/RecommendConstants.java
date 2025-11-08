package com.lyw.cloudRecommend.constant;

import java.math.BigDecimal;

/**
 * 推荐系统常量
 */
public class RecommendConstants {

    // 策略类型
    public static final String STRATEGY_COLLABORATIVE = "COLLABORATIVE";
    public static final String STRATEGY_CONTENT = "CONTENT";
    public static final String STRATEGY_HYBRID = "HYBRID";
    public static final String STRATEGY_REAL_TIME = "REAL_TIME";

    // 推荐类型
    public static final String RECOMMEND_HOME_PAGE = "HOME_PAGE";
    public static final String RECOMMEND_COURSE_DETAIL = "COURSE_DETAIL";
    public static final String RECOMMEND_PERSONALIZED = "PERSONALIZED";

    // 行为类型
    public static final String BEHAVIOR_VIEW = "VIEW";
    public static final String BEHAVIOR_ENROLL = "ENROLL";
    public static final String BEHAVIOR_COMPLETE = "COMPLETE";
    public static final String BEHAVIOR_LIKE = "LIKE";
    public static final String BEHAVIOR_RATE = "RATE";
    public static final String BEHAVIOR_SEARCH = "SEARCH";
    public static final String BEHAVIOR_DROP = "DROP";
    public static final String BEHAVIOR_WAITLIST_ENROLL = "WAITLIST_ENROLL";
    public static final String BEHAVIOR_LIKE_REVIEW = "LIKE_REVIEW";
    public static final String BEHAVIOR_UNLIKE_REVIEW = "UNLIKE_REVIEW";
    public static final String BEHAVIOR_USEFUL_REVIEW = "USEFUL_REVIEW";
    public static final String BEHAVIOR_UNUSEFUL_REVIEW = "UNUSEFUL_REVIEW";
    public static final String BEHAVIOR_LIKE_REPLY = "LIKE_REPLY";
    public static final String BEHAVIOR_UNLIKE_REPLY = "UNLIKE_REPLY";
    // 行为权重
    public static final BigDecimal WEIGHT_VIEW = new BigDecimal("0.1");
    public static final BigDecimal WEIGHT_LIKE = new BigDecimal("0.3");
    public static final BigDecimal WEIGHT_ENROLL = new BigDecimal("0.5");
    public static final BigDecimal WEIGHT_COMPLETE = new BigDecimal("0.8");
    public static final BigDecimal WEIGHT_RATE = new BigDecimal("0.4");
    public static final BigDecimal WEIGHT_DROP = new BigDecimal("-0.5");
    public static final BigDecimal WEIGHT_WAITLIST_ENROLL = new BigDecimal("0.6");
    public static final BigDecimal WEIGHT_LIKE_REVIEW = new BigDecimal("0.3");
    public static final BigDecimal WEIGHT_UNLIKE_REVIEW = new BigDecimal("-0.3");
    public static final BigDecimal WEIGHT_USEFUL_REVIEW = new BigDecimal("0.4");
    public static final BigDecimal WEIGHT_UNUSEFUL_REVIEW = new BigDecimal("-0.4");
    public static final BigDecimal WEIGHT_LIKE_REPLY = new BigDecimal("0.2");
    public static final BigDecimal WEIGHT_UNLIKE_REPLY = new BigDecimal("-0.2");
    // 配置状态
    public static final Integer STATUS_ACTIVE = 1;
    public static final Integer STATUS_INACTIVE = 0;

    // 默认推荐数量
    public static final int DEFAULT_RECOMMEND_SIZE = 20;
    public static final int MAX_RECOMMEND_SIZE = 100;
}