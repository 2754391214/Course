package com.lyw.cloudRanking.job;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.commonUtil.constant.CommonKeyConstant;
import com.lyw.commonUtil.constant.RedisKeyConstant;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import com.lyw.commonUtil.util.RedisUtils;
import com.lyw.commonUtil.util.TypeConversionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class DailySnapshotJob {

    @Resource
    private CourseHeatDailyBo courseHeatDailyBo;
    @Resource
    private RedisUtils redisUtils;
    /**
     * 每日凌晨生成热度快照
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void generateDailySnapshot() {
        log.info("开始生成每日热度快照");

        try {
            Set<ZSetOperations.TypedTuple<Object>> tuples = redisUtils.reverseRangeWithScores(RedisKeyConstant.RANKING_COURSE_HEAT, 0, -1);

            if (CollectionUtil.isEmpty(tuples)) {
                log.warn("没有找到排行榜数据");
                return;
            }

            Date snapshotDate = new Date();
            int rank = 1;

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                Long courseId = TypeConversionUtil.toLong(tuple.getValue());
                Double heatScore = tuple.getScore();

                // 保存每日快照
                CourseHeatDailyVo daily = new CourseHeatDailyVo();
                daily.setRankingCode(CommonKeyConstant.COURSE_HEAT);
                daily.setCourseId(courseId);
                daily.setHeatDate(snapshotDate);
                daily.setTotalHeat(BigDecimal.valueOf(heatScore));
                daily.setDailyRank(rank);
                daily.setCruAndLuu(CurUserUtil.getUserId());
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