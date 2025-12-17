package com.lyw.cloudRecommend.utils;

import com.lyw.commonUtil.constant.CommonKeyConstant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 推荐系统工具类
 */
public final class RecommendUtils {

    // 私有构造函数防止实例化
    private RecommendUtils() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

    // 用户行为评分映射（不可修改）
    private static final Map<String, Integer> USER_BEHAVIOR_SCORE_MAP;

    static {
        Map<String, Integer> tempMap = new HashMap<>();
        tempMap.put(CommonKeyConstant.COMMENT, 8);  // 注意：COMMENT被覆盖了，原值为5
        tempMap.put(CommonKeyConstant.DROP, -4);
        tempMap.put(CommonKeyConstant.LIKE, 4);
        tempMap.put(CommonKeyConstant.UNLIKE, -3);
        tempMap.put(CommonKeyConstant.FAVORITE, 4);
        tempMap.put(CommonKeyConstant.UNFAVORITE, -3);
        tempMap.put(CommonKeyConstant.REVIEW, 1);
        tempMap.put(CommonKeyConstant.REPLY, 2);

        USER_BEHAVIOR_SCORE_MAP = Collections.unmodifiableMap(tempMap);
    }

    /**
     * 获取用户行为评分映射
     * @return 不可修改的用户行为评分映射
     */
    public static Map<String, Integer> getUserBehaviorScoreMap() {
        return USER_BEHAVIOR_SCORE_MAP;
    }

    /**
     * 根据行为类型获取评分
     * @param behaviorType 行为类型
     * @return 对应的评分，如果不存在则返回0
     */
    public static int getBehaviorScore(String behaviorType) {
        return USER_BEHAVIOR_SCORE_MAP.getOrDefault(behaviorType, 0);
    }

    /**
     * 判断行为类型是否存在
     * @param behaviorType 行为类型
     * @return 是否存在
     */
    public static boolean containsBehavior(String behaviorType) {
        return USER_BEHAVIOR_SCORE_MAP.containsKey(behaviorType);
    }

    /**
     * 获取所有支持的行为类型
     * @return 行为类型集合
     */
    public static Set<String> getAllBehaviorTypes() {
        return USER_BEHAVIOR_SCORE_MAP.keySet();
    }

    /**
     * 计算行为总分
     * @param behaviors 行为及其次数
     * @return 总分
     */
    public static int calculateTotalScore(Map<String, Integer> behaviors) {
        if (behaviors == null || behaviors.isEmpty()) {
            return 0;
        }

        int totalScore = 0;
        for (Map.Entry<String, Integer> entry : behaviors.entrySet()) {
            int score = getBehaviorScore(entry.getKey());
            totalScore += score * entry.getValue();
        }
        return totalScore;
    }

    /**
     * 验证行为是否有效（评分是否在合理范围内）
     * @param behaviorType 行为类型
     * @return 是否有效
     */
    public static boolean isValidBehavior(String behaviorType) {
        Integer score = USER_BEHAVIOR_SCORE_MAP.get(behaviorType);
        return score != null && score != 0;
    }
}