package com.lyw.cloudRanking.service.impl;

import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.cloudRanking.dto.CourseHeatDailyDto;
import com.lyw.cloudRanking.mapper.CourseHeatDailyDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 课程热度日快照表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Service
public class CourseHeatDailyImpl extends BaseImpl<CourseHeatDailyDao, CourseHeatDailyVo, CourseHeatDailyDto> implements CourseHeatDailyBo {
    /**
     * 获取课程最近的热度数据
     */
    public List<CourseHeatDailyVo> getRecentHeatData(Long courseId, String period) {
        // 根据period参数计算时间范围
        Date startDate = calculateStartDate(period);

        return lambdaQuery()
                .eq(CourseHeatDailyVo::getCourseId, courseId)
                .ge(CourseHeatDailyVo::getHeatDate, startDate)
                .orderByAsc(CourseHeatDailyVo::getHeatDate)
                .list();
    }

    /**
     * 根据周期计算开始日期
     */
    private Date calculateStartDate(String period) {
        // 简化实现，实际应该根据period计算
        return new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L); // 7天前
    }

    /**
     * 获取指定日期的排行榜快照
     */
    public List<CourseHeatDailyVo> getRankingSnapshot(String rankingCode, Date date) {
        return lambdaQuery()
                .eq(CourseHeatDailyVo::getRankingCode, rankingCode)
                .eq(CourseHeatDailyVo::getHeatDate, date)
                .orderByAsc(CourseHeatDailyVo::getDailyRank)
                .list();
    }
}