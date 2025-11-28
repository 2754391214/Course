package com.lyw.cloudRanking.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudRanking.dto.*;
import com.lyw.cloudRanking.feign.CourseFeignService;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RankingQueryServiceImpl implements RankingQueryService {


    @Resource
    private CourseHeatDailyBo courseHeatDailyBo;
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private CourseFeignService courseFeignService;
    @Resource
    private DistributedCacheHelper distributedCacheHelper;
    /**
     * 获取实时排行榜
     */
    @Override
    public CourseResponseWrapper getRealTimeRanking(String rankingCode, int page, int size) {
        try {
            int start = (page - 1) * size;
            int end = start + size - 1;
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtils.reverseRangeWithScores(RedisKeyConstant.RANKING_COURSE_HEAT, start, end);
            if (CollectionUtil.isEmpty(tuples)) {
                return CourseResponseWrapper.getFailed("没有查询到排行榜数据");
            }

            List<RankingItemDto> ranking = new ArrayList<>();
            int rank = start + 1;

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long courseId = (Long) tuple.getValue();
                Double heatScore = tuple.getScore();

                ranking.add(RankingItemDto.builder()
                        .courseId(courseId)
                        .heatScore(heatScore)
                        .rank(rank++)
                        .courseInfo(getCourseCacheInfo(courseId))
                        .build());
            }

            return CourseResponseWrapper.getSuccess(ranking);

        } catch (Exception e) {
            log.error("获取实时排行榜失败: {}", rankingCode, e);
            return CourseResponseWrapper.getFailed("获取实时排行榜失败");
        }
    }

    /**
     * 获取课程排行榜详情
     */
    public CourseResponseWrapper getCourseRankingDetail(String rankingCode, Long courseId) {
        String heatKey = String.format(RedisKeyConstant.COURSE_HEAT_WHO, courseId);

        try {
            // 获取当前排名
            Long rank = redisUtils.zReverseRank(RedisKeyConstant.RANKING_COURSE_HEAT, courseId.toString());
            Double heatScore = redisUtils.get(heatKey);

            if (ObjectUtil.isEmpty(rank) || ObjectUtil.isEmpty(heatScore)) {
                return CourseResponseWrapper.getFailed("课程不在排行榜中");
            }

            return CourseResponseWrapper.getSuccess(CourseRankingDetailDto.builder()
                    .courseId(courseId)
                    .rankingCode(rankingCode)
                    .heatScore(heatScore)
                    .currentRank(rank.intValue() + 1) // 转为1-based排名
                    .courseInfo(getCourseCacheInfo(courseId))
                    .build());

        } catch (Exception e) {
            log.error("获取课程排行榜详情失败: courseId={}", courseId, e);
            return null;
        }
    }

    /**
     * 获取课程热度趋势
     */
    public CourseResponseWrapper getCourseHeatTrend(String rankingCode, Long courseId, String period) {
        // 从数据库查询历史数据
        List<CourseHeatDailyVo> dailyData = courseHeatDailyBo.getRecentHeatData(courseId, period);

        List<HeatDataPointDto> dataPoints = dailyData.stream()
                .map(daily -> HeatDataPointDto.builder()
                        .date(daily.getHeatDate())
                        .heatValue(daily.getTotalHeat().doubleValue())
                        .rank(daily.getDailyRank())
                        .build())
                .collect(Collectors.toList());

        return CourseResponseWrapper.getSuccess(HeatTrendDto.builder()
                .courseId(courseId)
                .period(period)
                .dataPoints(dataPoints)
                .build());
    }

    /**
     * 搜索排行榜课程
     */
    public CourseResponseWrapper searchRanking(String rankingCode, String keyword, int page, int size) {
        // 先获取整个排行榜，然后过滤
        List<RankingItemDto> allRanking = FeignResponseHelper.convertToList(getRealTimeRanking(rankingCode, 1, 1000),RankingItemDto.class);
        return CourseResponseWrapper.getSuccess(allRanking.stream()
                .filter(item -> matchesKeyword(item, keyword))
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList()));
    }

    private boolean matchesKeyword(RankingItemDto item, String keyword) {
        return item.getCourseInfo() != null &&
                item.getCourseInfo().getCourseName().toLowerCase().contains(keyword.toLowerCase());
    }


    // 课程信息缓存
    private CoursesDto getCachedCourse(Long courseId) {
        return distributedCacheHelper.getOrLoad(
                courseId.toString(),
                new CacheConfig(
                        RedisKeyConstant.COURSE_INFO,
                        RedisKeyConstant.LOCK_COURSE_INFO,
                        Duration.ofHours(24)
                ),
                () -> FeignResponseHelper.convert(
                        courseFeignService.searchDetail(courseId), CoursesDto.class),
                CoursesDto.class
        );
    }

    // 获取课程基本信息
    private CourseBasicInfoDto getCourseCacheInfo(Long courseId) {
        CoursesDto coursesDto = getCachedCourse(courseId);
        if (ObjectUtil.isEmpty(coursesDto)){
            return null;
        }
        return CourseBasicInfoDto
                .builder()
                .courseId(courseId)
                .courseName(coursesDto.getName())
                .teacherName(coursesDto.getTeacherName())
                .build();
    }
}