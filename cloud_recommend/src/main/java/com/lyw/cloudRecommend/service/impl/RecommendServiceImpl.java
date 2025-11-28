package com.lyw.cloudRecommend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudRecommend.dto.UserBehaviorMessage;
import com.lyw.cloudRecommend.engine.RecommendationEngine;
import com.lyw.cloudRecommend.constant.RecommendConstants;
import com.lyw.cloudRecommend.mapper.*;
import com.lyw.cloudRecommend.service.RecommendService;
import com.lyw.cloudRecommend.vo.*;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 */
@Slf4j
@Service
public class RecommendServiceImpl extends ServiceImpl<RecommendResultsDao, RecommendResultsVo> implements RecommendService {

    @Autowired
    private RecommendationEngine recommendationEngine;

    @Autowired
    private RecommendConfigDao recommendConfigMapper;

    @Autowired
    private UserBehaviorDao userBehaviorMapper;

    @Autowired
    private UserProfileDao userProfileMapper;

    @Autowired
    private RecommendResultsDao recommendResultsMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${recommend.real-time.window:3600000}") // 1小时
    private long realTimeWindow;

    @Value("${recommend.cache.expire-hours:1}")
    private int cacheExpireHours;

    @Override
    public CourseResponseWrapper<List<Long>> getPersonalizedRecommendation(Long userId, String recommendType, int size) {
        try {
            // 参数校验
            if (ObjectUtil.isEmpty(userId) || StrUtil.isBlank(recommendType)) {
                return CourseResponseWrapper.getFailed("参数不能为空");
            }

            size = Math.min(size, RecommendConstants.MAX_RECOMMEND_SIZE);

            // 检查是否有缓存的推荐结果
            List<Long> cachedResult = getCachedRecommendation(userId, recommendType);
            if (!cachedResult.isEmpty()) {
                log.info("返回缓存推荐结果, userId: {}, type: {}, size: {}", userId, recommendType, cachedResult.size());
                return CourseResponseWrapper.getSuccess(cachedResult);
            }

            // 获取活跃策略
            List<RecommendConfigVo> activeStrategies = getActiveStrategies().getData();
            if (activeStrategies.isEmpty()) {
                log.warn("没有可用的推荐策略, 返回热门推荐");
                return CourseResponseWrapper.getSuccess(getPopularRecommendations(size));
            }

            // 执行推荐
            Map<String, Object> recommendationResult = recommendationEngine.executeRecommendation(
                    userId, recommendType, activeStrategies, size);

            @SuppressWarnings("unchecked")
            List<Long> recommendations = (List<Long>) recommendationResult.get("recommendations");

            if (CollectionUtils.isEmpty(recommendations)) {
                log.warn("推荐结果为空, 返回热门推荐, userId: {}", userId);
                recommendations = getPopularRecommendations(size);
            }

            // 缓存推荐结果
            cacheRecommendationResult(userId, recommendType, recommendationResult);

            return CourseResponseWrapper.getSuccess(recommendations);

        } catch (Exception e) {
            log.error("个性化推荐失败, userId: {}, type: {}", userId, recommendType, e);
            // 降级方案：返回热门推荐
            return CourseResponseWrapper.getSuccess(getPopularRecommendations(size));
        }
    }

    @Override
    public CourseResponseWrapper<List<Long>> getRealTimeRecommendation(Long userId, int size) {
        try {
            // 基于用户最近行为实时推荐
            List<UserBehaviorVo> recentBehaviors = getRecentUserBehaviors(userId);

            if (recentBehaviors.isEmpty()) {
                log.info("用户无最近行为, 返回个性化推荐, userId: {}", userId);
                return getPersonalizedRecommendation(userId, RecommendConstants.RECOMMEND_PERSONALIZED, size);
            }

            // 实时推荐逻辑
            List<Long> realTimeRecommendations = generateRealTimeRecommendations(userId, recentBehaviors, size);

            if (CollectionUtils.isEmpty(realTimeRecommendations)) {
                log.info("实时推荐结果为空, 返回个性化推荐, userId: {}", userId);
                return getPersonalizedRecommendation(userId, RecommendConstants.RECOMMEND_PERSONALIZED, size);
            }

            return CourseResponseWrapper.getSuccess(realTimeRecommendations);

        } catch (Exception e) {
            log.error("实时推荐失败, userId: {}", userId, e);
            return CourseResponseWrapper.getFailed("实时推荐服务异常");
        }
    }

    @Override
    @Transactional
    public CourseResponseWrapper<Boolean> processUserBehavior(UserBehaviorVo userBehavior) {
        try {
            log.debug("处理用户行为, userId: {}, courseId: {}, behavior: {}",
                    userBehavior.getUserId(), userBehavior.getCourseId(), userBehavior.getBehaviorType());
            // 设置默认权重
            if (ObjectUtil.isEmpty(userBehavior.getBehaviorWeight())) {
                userBehavior.setBehaviorWeight(getDefaultBehaviorWeight(userBehavior.getBehaviorType()));
            }

            // 设置行为时间
            if (ObjectUtil.isEmpty(userBehavior.getBehaviorTime())) {
                userBehavior.setBehaviorTime(new Date());
            }

            // 保存用户行为
            int result = userBehaviorMapper.insert(userBehavior);
            if (result <= 0) {
                return CourseResponseWrapper.getFailed("保存用户行为失败");
            }

            // 异步更新用户画像
            updateUserProfileAsync(userBehavior.getUserId());

            // 实时更新推荐结果
            refreshRealTimeRecommendation(userBehavior.getUserId());

            log.debug("用户行为处理完成, userId: {}, behaviorId: {}",
                    userBehavior.getUserId(), userBehavior.getId());
            return CourseResponseWrapper.getSuccess(true);

        } catch (Exception e) {
            log.error("处理用户行为失败", e);
            return CourseResponseWrapper.getFailed("用户行为处理失败");
        }
    }

    @Override
    public CourseResponseWrapper<Boolean> updateUserProfile(Long userId) {
        try {
            // 基于用户行为更新画像
            UserProfileVo userProfile = calculateUserProfile(userId);

            // 保存或更新用户画像
            QueryWrapper<UserProfileVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            UserProfileVo existingProfile = userProfileMapper.selectOne(queryWrapper);

            if (ObjectUtil.isNotEmpty(existingProfile)) {
                userProfile.setId(existingProfile.getId());
                int updateResult = userProfileMapper.updateById(userProfile);
                if (updateResult <= 0) {
                    log.warn("更新用户画像失败, userId: {}", userId);
                }
            } else {
                int insertResult = userProfileMapper.insert(userProfile);
                if (insertResult <= 0) {
                    log.warn("插入用户画像失败, userId: {}", userId);
                }
            }
            return CourseResponseWrapper.getSuccess(true);

        } catch (Exception e) {
            log.error("更新用户画像失败, userId: {}", userId, e);
            return CourseResponseWrapper.getFailed("用户画像更新失败");
        }
    }

    @Override
    public CourseResponseWrapper<List<RecommendConfigVo>> getActiveStrategies() {
        try {
            QueryWrapper<RecommendConfigVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", RecommendConstants.STATUS_ACTIVE)
                    .orderByDesc("weight");
            List<RecommendConfigVo> strategies = recommendConfigMapper.selectList(queryWrapper);

            return CourseResponseWrapper.getSuccess(strategies);

        } catch (Exception e) {
            log.error("获取推荐策略失败", e);
            return CourseResponseWrapper.getFailed("获取推荐策略失败");
        }
    }

    @Override
    public CourseResponseWrapper<Boolean> batchGenerateRecommendations() {
        try {
            // 批量为用户生成推荐结果
            List<Long> activeUserIds = getActiveUserIds();
            List<RecommendConfigVo> activeStrategies = getActiveStrategies().getData();

            if (CollectionUtils.isEmpty(activeStrategies)) {
                return CourseResponseWrapper.getFailed("没有可用的推荐策略");
            }

            log.info("开始批量生成推荐, 用户数量: {}", activeUserIds.size());

            int successCount = 0;
            for (Long userId : activeUserIds) {
                try {
                    Map<String, Object> recommendationResult = recommendationEngine.executeRecommendation(
                            userId, RecommendConstants.RECOMMEND_PERSONALIZED,
                            activeStrategies, RecommendConstants.DEFAULT_RECOMMEND_SIZE);

                    @SuppressWarnings("unchecked")
                    List<Long> recommendations = (List<Long>) recommendationResult.get("recommendations");

                    if (!CollectionUtils.isEmpty(recommendations)) {
                        cacheRecommendationResult(userId, RecommendConstants.RECOMMEND_PERSONALIZED, recommendationResult);
                        successCount++;
                    }

                    // 每处理100个用户记录一次日志
                    if (successCount % 100 == 0) {
                        log.info("批量推荐进度: {}/{}", successCount, activeUserIds.size());
                    }

                } catch (Exception e) {
                    log.error("批量推荐失败, userId: {}", userId, e);
                }
            }

            log.info("批量生成推荐完成, 成功: {}, 总数: {}", successCount, activeUserIds.size());
            return CourseResponseWrapper.getSuccess(true);

        } catch (Exception e) {
            log.error("批量生成推荐失败", e);
            return CourseResponseWrapper.getFailed("批量推荐失败");
        }
    }

    @Override
    public void handleUserBehaviorMessage(UserBehaviorMessage message) {
        try {
            log.info("接收到用户行为消息, messageId: {}, userId: {}, courseId: {}, behavior: {}",
                    message.getMessageId(), message.getUserId(), message.getCourseId(), message.getBehaviorType());

            // 参数验证
            if (!validateMessage(message)) {
                log.error("消息参数验证失败, messageId: {}", message.getMessageId());
                throw new IllegalArgumentException("消息参数不合法");
            }

            // 转换为推荐模块内部的 VO 对象
            UserBehaviorVo userBehavior = convertToUserBehaviorVo(message);

            // 处理用户行为
            CourseResponseWrapper<Boolean> result = processUserBehavior(userBehavior);

            if (!result.isSuccess()) {
                log.error("处理用户行为失败, messageId: {}, error: {}",
                        message.getMessageId(), result.getErrorMessage());
                throw new RuntimeException("处理用户行为失败: " + result.getErrorMessage());
            }

            log.info("用户行为消息处理成功, messageId: {}, userId: {}, behavior: {}",
                    message.getMessageId(), message.getUserId(), message.getBehaviorType());

        } catch (Exception e) {
            log.error("处理用户行为消息异常, messageId: {}", message.getMessageId(), e);
            // 重新抛出异常，让消息进入死信队列
            throw new RuntimeException("用户行为消息处理异常", e);
        }
    }

    @Override
    public void handleFailedUserBehaviorMessage(UserBehaviorMessage message) {
        try {
            log.warn("处理死信队列中的用户行为消息, messageId: {}, userId: {}, behavior: {}",
                    message.getMessageId(), message.getUserId(), message.getBehaviorType());

            // 1. 记录错误日志
            logErrorBehavior(message);

            // 2. 尝试降级处理（可选）
            attemptFallbackProcessing(message);

            // 3. 发送告警通知（可选）
            sendAlertNotification(message);

        } catch (Exception e) {
            log.error("处理死信队列消息异常, messageId: {}", message.getMessageId(), e);
        }
    }

    // ==================== 私有方法具体实现 ====================
    /**
     * 消息参数验证
     */
    private boolean validateMessage(UserBehaviorMessage message) {
        if (message == null) {
            return false;
        }
        if (message.getUserId() == null || message.getUserId() <= 0) {
            log.error("用户ID不合法: {}", message.getUserId());
            return false;
        }
        if (message.getCourseId() == null || message.getCourseId() <= 0) {
            log.error("课程ID不合法: {}", message.getCourseId());
            return false;
        }
        if (message.getBehaviorType() == null || message.getBehaviorType().trim().isEmpty()) {
            log.error("行为类型不能为空");
            return false;
        }
        if (message.getBehaviorTime() == null) {
            log.error("行为时间不能为空");
            return false;
        }
        return true;
    }

    /**
     * 转换为 UserBehaviorVo
     */
    private UserBehaviorVo convertToUserBehaviorVo(UserBehaviorMessage message) {
        UserBehaviorVo userBehavior = new UserBehaviorVo();
        userBehavior.setUserId(message.getUserId());
        userBehavior.setCourseId(message.getCourseId());
        userBehavior.setBehaviorType(message.getBehaviorType());
        userBehavior.setBehaviorTime(message.getBehaviorTime());
        userBehavior.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        userBehavior.setCruAndLuu(CurUserUtil.getUserId());
        // 设置行为权重（如果消息中未提供，则根据行为类型计算）
        if (message.getBehaviorWeight() != null) {
            userBehavior.setBehaviorWeight(message.getBehaviorWeight());
        } else {
            userBehavior.setBehaviorWeight(getDefaultBehaviorWeight(message.getBehaviorType()));
        }

        // 设置上下文信息
        if (message.getContext() != null) {
            // 将 Map 转换为 JSON 字符串
            // userBehavior.setContext(JsonUtils.toJsonString(message.getContext()));
        }

        return userBehavior;
    }
    /**
     * 记录错误行为日志
     */
    private void logErrorBehavior(UserBehaviorMessage message) {
        // 实现错误日志记录逻辑
        // 可以写入数据库错误表或日志文件
        log.error("用户行为处理失败记录 - messageId: {}, userId: {}, courseId: {}, behavior: {}",
                message.getMessageId(), message.getUserId(), message.getCourseId(), message.getBehaviorType());
    }
    /**
     * 尝试降级处理
     */
    private void attemptFallbackProcessing(UserBehaviorMessage message) {
        try {
            // 简化处理：只记录基础信息，不进行复杂的推荐计算
            log.info("降级处理用户行为: userId: {}, behavior: {}",
                    message.getUserId(), message.getBehaviorType());

            // 可以在这里实现一些基本的处理逻辑
            // 比如只更新用户画像，不重新计算推荐结果

        } catch (Exception e) {
            log.error("降级处理失败, messageId: {}", message.getMessageId(), e);
        }
    }

    /**
     * 发送告警通知
     */
    private void sendAlertNotification(UserBehaviorMessage message) {
        // 实现告警通知逻辑
        // 可以发送邮件、短信、钉钉通知等
        log.warn("发送用户行为处理失败告警, messageId: {}, userId: {}",
                message.getMessageId(), message.getUserId());
    }


    /**
     * 获取缓存的推荐结果
     */
    private List<Long> getCachedRecommendation(Long userId, String recommendType) {
        try {
            QueryWrapper<RecommendResultsVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .eq("recommend_type", recommendType)
                    .gt("expire_time", new Date())
                    .orderByDesc("create_time")
                    .last("LIMIT 1");

            RecommendResultsVo cachedResult = recommendResultsMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotEmpty(cachedResult) && StringUtils.hasText(cachedResult.getCourseIds())) {
                List<Long> courseIds = parseCourseIds(cachedResult.getCourseIds());
                log.debug("命中缓存推荐, userId: {}, type: {}, 数量: {}", userId, recommendType, courseIds.size());
                return courseIds;
            }
        } catch (Exception e) {
            log.error("获取缓存推荐失败, userId: {}, type: {}", userId, recommendType, e);
        }
        return new ArrayList<>();
    }

    /**
     * 缓存推荐结果
     */
    private void cacheRecommendationResult(Long userId, String recommendType, Map<String, Object> result) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> recommendations = (List<Long>) result.get("recommendations");
            @SuppressWarnings("unchecked")
            Map<Long, Double> scores = (Map<Long, Double>) result.get("scores");
            @SuppressWarnings("unchecked")
            List<String> strategies = (List<String>) result.get("strategies");

            if (CollectionUtils.isEmpty(recommendations)) {
                return;
            }

            RecommendResultsVo recommendResult = new RecommendResultsVo()
                    .setUserId(userId)
                    .setRecommendType(recommendType)
                    .setCourseIds(formatCourseIds(recommendations))
                    .setScores(formatScores(scores))
                    .setStrategyUsed(String.join(",", strategies))
                    .setExpireTime(calculateExpireTime());

            // 删除旧的推荐结果
            QueryWrapper<RecommendResultsVo> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("user_id", userId).eq("recommend_type", recommendType);
            recommendResultsMapper.delete(deleteWrapper);

            // 插入新的推荐结果
            int insertResult = recommendResultsMapper.insert(recommendResult);
            if (insertResult > 0) {
                log.debug("缓存推荐结果成功, userId: {}, type: {}, 数量: {}", userId, recommendType, recommendations.size());
            }

        } catch (Exception e) {
            log.error("缓存推荐结果失败, userId: {}, type: {}", userId, recommendType, e);
        }
    }

    /**
     * 获取最近用户行为
     */
    private List<UserBehaviorVo> getRecentUserBehaviors(Long userId) {
        try {
            Date timeThreshold = new Date(System.currentTimeMillis() - realTimeWindow);

            QueryWrapper<UserBehaviorVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .ge("behavior_time", timeThreshold)
                    .orderByDesc("behavior_time")
                    .last("LIMIT 100");
            return userBehaviorMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取最近用户行为失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 生成实时推荐
     */
    private List<Long> generateRealTimeRecommendations(Long userId, List<UserBehaviorVo> recentBehaviors, int size) {
        try {
            // 1. 分析最近行为模式
            Map<String, Object> realTimeInterests = analyzeRealTimeInterests(recentBehaviors);

            // 2. 基于行为类型和权重计算实时偏好
            Map<Long, Double> candidateScores = new HashMap<>();

            for (UserBehaviorVo behavior : recentBehaviors) {
                // 根据行为类型和权重计算相关课程的分数
                List<Long> relatedCourses = findRelatedCourses(behavior);
                double baseScore = behavior.getBehaviorWeight().doubleValue();

                for (Long courseId : relatedCourses) {
                    double timeDecay = calculateTimeDecay(behavior.getBehaviorTime());
                    double finalScore = baseScore * timeDecay;
                    candidateScores.merge(courseId, finalScore, Double::sum);
                }
            }

            // 3. 排除用户已经交互过的课程
            Set<Long> interactedCourses = getInteractedCourses(userId);
            candidateScores.keySet().removeAll(interactedCourses);

            // 4. 排序并返回结果
            return candidateScores.entrySet().stream()
                    .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                    .limit(size)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("生成实时推荐失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 刷新实时推荐结果
     */
    private void refreshRealTimeRecommendation(Long userId) {
        try {
            // 异步刷新实时推荐
            refreshRealTimeAsync(userId);
        } catch (Exception e) {
            log.error("刷新实时推荐失败, userId: {}", userId, e);
        }
    }

    /**
     * 计算用户画像
     */
    private UserProfileVo calculateUserProfile(Long userId) {
        try {
            // 1. 获取用户历史行为
            List<UserBehaviorVo> userBehaviors = getUserAllBehaviors(userId);
            if (CollectionUtils.isEmpty(userBehaviors)) {
                return createDefaultUserProfile(userId);
            }

            // 2. 计算兴趣标签
            String interestTags = calculateInterestTags(userBehaviors);

            // 3. 分析行为模式
            String behaviorPattern = analyzeBehaviorPattern(userBehaviors);

            // 4. 提取偏好分类
            String preferredCategories = extractPreferredCategories(userBehaviors);

            // 5. 分析难度偏好
            String preferredDifficulty = analyzeDifficultyPreference(userBehaviors);

            // 6. 提取教师偏好
            String preferredTeachers = extractTeacherPreference(userBehaviors);

            // 7. 推断学习目标
            String learningGoals = inferLearningGoals(userBehaviors);

            // 8. 计算特征向量
            String featureVector = calculateFeatureVector(userBehaviors);
            UserProfileVo userProfileVo = new UserProfileVo()
                    .setUserId(userId)
                    .setInterestTags(interestTags)
                    .setBehaviorPattern(behaviorPattern)
                    .setPreferredCategories(preferredCategories)
                    .setPreferredDifficulty(preferredDifficulty)
                    .setPreferredTeachers(preferredTeachers)
                    .setLearningGoals(learningGoals)
                    .setFeatureVector(featureVector);
            userProfileVo.setCruAndLuu(CurUserUtil.getUserId());
            userProfileVo.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            return userProfileVo;

        } catch (Exception e) {
            log.error("计算用户画像失败, userId: {}", userId, e);
            return createDefaultUserProfile(userId);
        }
    }

    /**
     * 获取活跃用户ID列表
     */
    private List<Long> getActiveUserIds() {
        try {
            // 获取最近30天有行为的用户
            Date activeThreshold = new Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000);

            QueryWrapper<UserBehaviorVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DISTINCT user_id")
                    .ge("behavior_time", activeThreshold)
                    .groupBy("user_id")
                    .having("COUNT(*) >= 3"); // 至少有3次行为

            List<UserBehaviorVo> behaviors = userBehaviorMapper.selectList(queryWrapper);
            return behaviors.stream()
                    .map(UserBehaviorVo::getUserId)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("获取活跃用户ID失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取默认行为权重
     */
    private BigDecimal getDefaultBehaviorWeight(String behaviorType) {
        switch (behaviorType) {
            case RecommendConstants.BEHAVIOR_VIEW:
                return RecommendConstants.WEIGHT_VIEW;
            case RecommendConstants.BEHAVIOR_LIKE:
                return RecommendConstants.WEIGHT_LIKE;
            case RecommendConstants.BEHAVIOR_ENROLL:
                return RecommendConstants.WEIGHT_ENROLL;
            case RecommendConstants.BEHAVIOR_COMPLETE:
                return RecommendConstants.WEIGHT_COMPLETE;
            case RecommendConstants.BEHAVIOR_RATE:
                return RecommendConstants.WEIGHT_RATE;
            case RecommendConstants.BEHAVIOR_DROP:
                return RecommendConstants.WEIGHT_DROP;
            case RecommendConstants.BEHAVIOR_WAITLIST_ENROLL:
                return RecommendConstants.WEIGHT_WAITLIST_ENROLL;
            case RecommendConstants.BEHAVIOR_LIKE_REVIEW:
                return RecommendConstants.WEIGHT_LIKE_REVIEW;
            case RecommendConstants.BEHAVIOR_UNLIKE_REVIEW:
                return RecommendConstants.WEIGHT_UNLIKE_REVIEW;
            case RecommendConstants.BEHAVIOR_USEFUL_REVIEW:
                return RecommendConstants.WEIGHT_USEFUL_REVIEW;
            case RecommendConstants.BEHAVIOR_UNUSEFUL_REVIEW:
                return RecommendConstants.WEIGHT_UNUSEFUL_REVIEW;
            case RecommendConstants.BEHAVIOR_LIKE_REPLY:
                return RecommendConstants.WEIGHT_LIKE_REPLY;
            case RecommendConstants.BEHAVIOR_UNLIKE_REPLY:
                return RecommendConstants.WEIGHT_UNLIKE_REPLY;

            case RecommendConstants.BEHAVIOR_SEARCH:
                return new BigDecimal("0.2");
            default:
                return new BigDecimal("0.1");
        }
    }

    /**
     * 解析课程ID字符串
     */
    private List<Long> parseCourseIds(String courseIdsStr) {
        if (!StringUtils.hasText(courseIdsStr)) {
            return new ArrayList<>();
        }
        try {
            return Arrays.stream(courseIdsStr.split(","))
                    .map(String::trim)
                    .filter(str -> !str.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("解析课程ID失败: {}", courseIdsStr, e);
            return new ArrayList<>();
        }
    }

    /**
     * 格式化课程ID列表
     */
    private String formatCourseIds(List<Long> courseIds) {
        if (CollectionUtils.isEmpty(courseIds)) {
            return "";
        }
        return courseIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 格式化分数
     */
    private String formatScores(Map<Long, Double> scores) {
        if (scores == null || scores.isEmpty()) {
            return "";
        }
        return scores.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + String.format("%.4f", entry.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * 计算过期时间
     */
    private Date calculateExpireTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, cacheExpireHours);
        return calendar.getTime();
    }

    /**
     * 获取热门推荐
     */
    private List<Long> getPopularRecommendations(int size) {
        try {
            // 这里应该查询热门课程，暂时返回空列表
            // 实际实现应该从课程服务获取热门课程
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("获取热门推荐失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 分析实时兴趣
     */
    private Map<String, Object> analyzeRealTimeInterests(List<UserBehaviorVo> recentBehaviors) {
        Map<String, Object> interests = new HashMap<>();

        // 分析最近的行为模式
        Map<String, Long> behaviorCount = recentBehaviors.stream()
                .collect(Collectors.groupingBy(UserBehaviorVo::getBehaviorType, Collectors.counting()));

        interests.put("behaviorDistribution", behaviorCount);

        // 提取频繁访问的课程类别
        Set<Long> recentCourseIds = recentBehaviors.stream()
                .map(UserBehaviorVo::getCourseId)
                .collect(Collectors.toSet());

        interests.put("recentCourseIds", recentCourseIds);

        return interests;
    }

    /**
     * 查找相关课程
     */
    private List<Long> findRelatedCourses(UserBehaviorVo behavior) {
        // 这里应该根据行为查找相关课程
        // 例如：基于课程相似度、同一类别、同一教师等
        // 暂时返回空列表
        return new ArrayList<>();
    }

    /**
     * 计算时间衰减
     */
    private double calculateTimeDecay(Date behaviorTime) {
        long timeDiff = System.currentTimeMillis() - behaviorTime.getTime();
        double hoursDiff = timeDiff / (1000.0 * 60 * 60);
        return Math.exp(-hoursDiff / 24.0); // 24小时衰减因子
    }

    /**
     * 获取用户交互过的课程
     */
    private Set<Long> getInteractedCourses(Long userId) {
        try {
            QueryWrapper<UserBehaviorVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("DISTINCT course_id")
                    .eq("user_id", userId);
            List<UserBehaviorVo> behaviors = userBehaviorMapper.selectList(queryWrapper);
            return behaviors.stream()
                    .map(UserBehaviorVo::getCourseId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("获取用户交互课程失败, userId: {}", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 获取用户所有行为
     */
    private List<UserBehaviorVo> getUserAllBehaviors(Long userId) {
        try {
            QueryWrapper<UserBehaviorVo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                    .orderByAsc("behavior_time");
            return userBehaviorMapper.selectList(queryWrapper);
        } catch (Exception e) {
            log.error("获取用户行为失败, userId: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 创建默认用户画像
     */
    private UserProfileVo createDefaultUserProfile(Long userId) {
        return new UserProfileVo()
                .setUserId(userId)
                .setInterestTags(null)
                .setBehaviorPattern(JSON.toJSONString("NEW_USER"))
                .setPreferredCategories(null)
                .setPreferredDifficulty("BEGINNER")
                .setPreferredTeachers(null)
                .setLearningGoals(null)
                .setFeatureVector(null);
    }

    /**
     * 计算兴趣标签
     */
    private String calculateInterestTags(List<UserBehaviorVo> userBehaviors) {
        // 实现兴趣标签计算逻辑
        // 基于用户行为分析兴趣点
        return JSON.toJSONString("");
    }

    /**
     * 分析行为模式
     */
    private String analyzeBehaviorPattern(List<UserBehaviorVo> userBehaviors) {
        // 分析用户行为模式：活跃时间、频率、偏好等
        return JSON.toJSONString("REGULAR");
    }

    /**
     * 提取偏好分类
     */
    private String extractPreferredCategories(List<UserBehaviorVo> userBehaviors) {
        // 提取用户偏好的课程分类
        return JSON.toJSONString("");
    }

    /**
     * 分析难度偏好
     */
    private String analyzeDifficultyPreference(List<UserBehaviorVo> userBehaviors) {
        // 分析用户偏好的难度级别
        return "MIXED";
    }

    /**
     * 提取教师偏好
     */
    private String extractTeacherPreference(List<UserBehaviorVo> userBehaviors) {
        // 提取用户偏好的教师
        return JSON.toJSONString("");
    }

    /**
     * 推断学习目标
     */
    private String inferLearningGoals(List<UserBehaviorVo> userBehaviors) {
        // 基于用户行为推断学习目标
        return JSON.toJSONString("");
    }

    /**
     * 计算特征向量
     */
    private String calculateFeatureVector(List<UserBehaviorVo> userBehaviors) {
        try {
            // 计算用户特征向量（用于机器学习模型）
            Map<String, Object> features = new HashMap<>();

            // 行为统计特征
            long totalBehaviors = userBehaviors.size();
            features.put("totalBehaviors", totalBehaviors);

            // 活跃度特征
            Map<String, Long> behaviorTypeCount = userBehaviors.stream()
                    .collect(Collectors.groupingBy(UserBehaviorVo::getBehaviorType, Collectors.counting()));
            features.put("behaviorTypeCount", behaviorTypeCount);

            return JSON.toJSONString(objectMapper.writeValueAsString(features));

        } catch (Exception e) {
            log.error("计算特征向量失败", e);
            return "{}";
        }
    }

    // ==================== 异步方法 ====================

    /**
     * 异步更新用户画像
     */
    @Async
    public void updateUserProfileAsync(Long userId) {
        try {
            updateUserProfile(userId);
            log.debug("异步更新用户画像完成, userId: {}", userId);
        } catch (Exception e) {
            log.error("异步更新用户画像失败, userId: {}", userId, e);
        }
    }

    /**
     * 异步刷新实时推荐
     */
    @Async
    public void refreshRealTimeAsync(Long userId) {
        try {
            // 生成新的实时推荐并缓存
            List<UserBehaviorVo> recentBehaviors = getRecentUserBehaviors(userId);
            if (!recentBehaviors.isEmpty()) {
                List<Long> realTimeRecommendations = generateRealTimeRecommendations(userId, recentBehaviors, 10);

                Map<String, Object> result = new HashMap<>();
                result.put("recommendations", realTimeRecommendations);
                result.put("scores", new HashMap<Long, Double>());
                result.put("strategies", Arrays.asList("REAL_TIME"));

                cacheRecommendationResult(userId, "REAL_TIME", result);
                log.debug("异步刷新实时推荐完成, userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("异步刷新实时推荐失败, userId: {}", userId, e);
        }
    }
}