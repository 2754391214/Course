package com.lyw.cloudRanking.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lyw.cloudRanking.dto.*;
import com.lyw.cloudRanking.feign.CourseFeignService;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.dto.CourseBasicInfoDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.FeignResponseHelper;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.TypeConversionUtil;
import com.lyw.commonUtil.util.reidsCache.StringCache.CacheConfig;
import com.lyw.commonUtil.util.reidsCache.StringCache.DistributedCacheHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
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
     * 获取课程实时排行榜
     */
    @Override
    public CourseResponseWrapper getCourseRanking(int page, int size) {
        try {
            int start = (page - 1) * size;
            int end = start + size - 1;
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtils.reverseRangeWithScores(RedisKeyConstant.RANKING_COURSE_HEAT, start, end);
            if (CollectionUtil.isEmpty(tuples)) {
                return CourseResponseWrapper.getFailed("没有查询到课程热度排行榜数据");
            }

            List<RankingItemDto> ranking = new ArrayList<>();
            int rank = start + 1;

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long courseId = TypeConversionUtil.toLong(tuple.getValue());
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
            log.error("获取课程实时排行榜失败: {}", e);
            return CourseResponseWrapper.getFailed("获取课程实时排行榜失败");
        }
    }

    /**
     * 获取课程热度趋势
     */
    public CourseResponseWrapper getCourseHeatTrend(Long courseId, String period) {
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