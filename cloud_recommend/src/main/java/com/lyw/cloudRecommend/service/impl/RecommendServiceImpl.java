package com.lyw.cloudRecommend.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudRecommend.feign.CourseFeignService;
import com.lyw.cloudRecommend.feign.RankingFeignService;
import com.lyw.cloudRecommend.mapper.RecommendResultsDao;
import com.lyw.cloudRecommend.service.RecommendService;
import com.lyw.cloudRecommend.service.UserBehaviorService;
import com.lyw.cloudRecommend.service.UserCFService;
import com.lyw.cloudRecommend.vo.RecommendResultsVo;
import com.lyw.commonUtil.message.UserBehaviorMessage;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 */
@Slf4j
@Service
public class RecommendServiceImpl extends ServiceImpl<RecommendResultsDao, RecommendResultsVo> implements RecommendService {
    @Resource
    private RecommendResultsDao recommendResultsDao;
    @Resource
    private UserBehaviorService userBehaviorService;
    @Resource
    private UserCFService userCFService;
    @Resource
    private RankingFeignService rankingFeignService;
    @Resource
    private CourseFeignService courseFeignService;

    @Override
    public CourseResponseWrapper getRecommendationList(Long userId, int size) {
        try {
            // 检查是否有缓存的推荐结果
            String cachedResult = getCachedRecommendation(userId);
            if (StrUtil.isNotEmpty(cachedResult)) {
                String[] split = cachedResult.split(",");
                List<Long> longList = Arrays.stream(split)
                        .map(String::trim) // 去除可能的空格
                        .filter(s -> !s.isEmpty()) // 过滤空字符串
                        .map(Long::parseLong) // 转换为 Long
                        .collect(Collectors.toList());
                List<Map> courseBasicInfoDtos = FeignResponseHelper.convertToList(courseFeignService.searchBatchByIds(longList), Map.class);
                return CourseResponseWrapper.getSuccess(courseBasicInfoDtos);
            }
            return rankingFeignService.getCourseRanking(1,size);

        } catch (Exception e) {
            // 降级方案：返回热门推荐
            return rankingFeignService.getCourseRanking(1,size);
        }
    }

    /**
     * 获取缓存的推荐结果
     */
    private String getCachedRecommendation(Long userId) {
        try {
            QueryWrapper<RecommendResultsVo> queryWrapper = new QueryWrapper<RecommendResultsVo>()
                    .eq("user_id", userId)
                    .orderByDesc("crd")
                    .last("LIMIT 1");

            RecommendResultsVo cachedResult = recommendResultsDao.selectOne(queryWrapper);
            if (ObjectUtil.isNotEmpty(cachedResult) && StrUtil.isNotEmpty(cachedResult.getCourseIds())) {
                return cachedResult.getCourseIds();
            }
        } catch (Exception e) {
            log.error("获取缓存推荐失败, userId: {}, ", userId,  e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleUserBehaviorMessage(UserBehaviorMessage message) {
        userBehaviorService.saveUserBehavior(message);
        userCFService.saveUserService(message);
    }

    @Override
    public void handleFailedUserBehaviorMessage(UserBehaviorMessage message) {
        try {
            log.warn("处理死信队列中的用户行为消息, messageId: {}, userId: {}, behavior: {}",
                    message.getMessageId(), message.getUserId(), message.getBehaviorType());

            // 记录错误日志
            logErrorBehavior(message);

            // 发送告警通知（可选）
            sendAlertNotification(message);

        } catch (Exception e) {
            log.error("处理死信队列消息异常, messageId: {}", message.getMessageId(), e);
        }
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
     * 发送告警通知
     */
    private void sendAlertNotification(UserBehaviorMessage message) {
        // 实现告警通知逻辑
        // 可以发送邮件、短信、钉钉通知等
        log.warn("发送用户行为处理失败告警, messageId: {}, userId: {}",
                message.getMessageId(), message.getUserId());
    }

}