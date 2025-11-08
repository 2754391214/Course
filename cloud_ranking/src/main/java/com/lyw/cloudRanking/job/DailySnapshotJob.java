package com.lyw.cloudRanking.job;

import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.service.RankingQueryService;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.CurUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class DailySnapshotJob {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CourseHeatDailyBo courseHeatDailyBo;

    @Autowired
    private RankingQueryService rankingQueryService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 每日凌晨生成热度快照
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void generateDailySnapshot() {
        log.info("开始生成每日热度快照");

        try {
            String rankingKey = "ranking:course:heat";
            Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                    .reverseRangeWithScores(rankingKey, 0, -1);

            if (tuples == null) {
                log.warn("没有找到排行榜数据");
                return;
            }

            Date snapshotDate = new Date();
            int rank = 1;

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                Long courseId = Long.valueOf(tuple.getValue());
                Double heatScore = tuple.getScore();

                // 保存每日快照
                CourseHeatDailyVo daily = new CourseHeatDailyVo()
                        .setRankingCode("course_heat")
                        .setCourseId(courseId)
                        .setHeatDate(snapshotDate)
                        .setTotalHeat(BigDecimal.valueOf(heatScore))
                        .setDailyRank(rank);
                daily.setCruAndLuu(CurUserUtil.getUserCode());
                daily.setCrdAndLud(DateTimeUtils.getCurrentDateTime());

                courseHeatDailyBo.save(daily);
                rank++;
            }

            log.info("每日热度快照生成完成: 共{}门课程", tuples.size());

        } catch (Exception e) {
            log.error("生成每日热度快照失败", e);
        }
    }
}