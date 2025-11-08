package com.lyw.cloudRanking.service.impl;

import com.lyw.cloudRanking.dto.*;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.service.RankingConfigBo;
import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingQueryServiceImpl implements RankingQueryService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CourseHeatDailyBo courseHeatDailyBo;

    @Autowired
    private RankingConfigBo rankingConfigBo;
    /**
     * 获取实时排行榜
     */
    public List<RankingItemDto> getRealTimeRanking(String rankingCode, int page, int size) {
        String rankingKey = "ranking:course:heat";

        try {
            int start = (page - 1) * size;
            int end = start + size - 1;

            Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(rankingKey, start, end);

            if (tuples == null) {
                return Collections.emptyList();
            }

            List<RankingItemDto> ranking = new ArrayList<>();
            int rank = start + 1;

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long courseId = Long.valueOf(tuple.getValue().toString());
                Double heatScore = tuple.getScore();

                RankingItemDto item = RankingItemDto.builder()
                        .courseId(courseId)
                        .heatScore(heatScore)
                        .rank(rank++)
                        .courseInfo(getCourseCacheInfo(courseId))
                        .build();

                ranking.add(item);
            }

            return ranking;

        } catch (Exception e) {
            log.error("获取实时排行榜失败: {}", rankingCode, e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取课程排行榜详情
     */
    public CourseRankingDetailDto getCourseRankingDetail(String rankingCode, Long courseId) {
        String rankingKey = "ranking:course:heat";
        String heatKey = String.format("course:heat:%d", courseId);

        try {
            // 获取当前排名
            Long rank = redisTemplate.opsForZSet().reverseRank(rankingKey, courseId.toString());
            Double heatScore = (Double) redisTemplate.opsForValue().get(heatKey);

            if (rank == null || heatScore == null) {
                return null;
            }

            return CourseRankingDetailDto.builder()
                    .courseId(courseId)
                    .rankingCode(rankingCode)
                    .heatScore(heatScore)
                    .currentRank(rank.intValue() + 1) // 转为1-based排名
                    .courseInfo(getCourseCacheInfo(courseId))
                    .build();

        } catch (Exception e) {
            log.error("获取课程排行榜详情失败: courseId={}", courseId, e);
            return null;
        }
    }

    /**
     * 获取课程热度趋势
     */
    public HeatTrendDto getCourseHeatTrend(String rankingCode, Long courseId, String period) {
        // 从数据库查询历史数据
        List<CourseHeatDailyVo> dailyData = courseHeatDailyBo.getRecentHeatData(courseId, period);

        List<HeatDataPointDto> dataPoints = dailyData.stream()
                .map(daily -> HeatDataPointDto.builder()
                        .date(daily.getHeatDate())
                        .heatValue(daily.getTotalHeat().doubleValue())
                        .rank(daily.getDailyRank())
                        .build())
                .collect(Collectors.toList());

        return HeatTrendDto.builder()
                .courseId(courseId)
                .period(period)
                .dataPoints(dataPoints)
                .build();
    }

    /**
     * 搜索排行榜课程
     */
    public List<RankingItemDto> searchRanking(String rankingCode, String keyword, int page, int size) {
        // 先获取整个排行榜，然后过滤（实际应该用Redis Search或其他搜索方案）
        List<RankingItemDto> allRanking = getRealTimeRanking(rankingCode, 1, 1000);

        return allRanking.stream()
                .filter(item -> matchesKeyword(item, keyword))
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    /**
     * 刷新排行榜缓存
     */
    public void refreshRankingCache(String rankingCode) {
        // 可以重新计算或触发缓存更新
        log.info("刷新排行榜缓存: {}", rankingCode);
    }

    private boolean matchesKeyword(RankingItemDto item, String keyword) {
        // 简化实现，实际应该查询课程信息
        return item.getCourseInfo() != null &&
                item.getCourseInfo().getCourseName().toLowerCase().contains(keyword.toLowerCase());
    }

    private CourseBasicInfoDto getCourseCacheInfo(Long courseId) {
        // 从缓存或外部服务获取课程基本信息
        // 简化实现，返回空对象
        return CourseBasicInfoDto.builder()
                .courseId(courseId)
                .courseName("课程" + courseId)
                .teacherName("教师")
                .build();
    }
}