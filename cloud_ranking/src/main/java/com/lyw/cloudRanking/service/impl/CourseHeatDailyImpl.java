package com.lyw.cloudRanking.service.impl;

import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.cloudRanking.dto.CourseHeatDailyDto;
import com.lyw.cloudRanking.mapper.CourseHeatDailyDao;
import com.lyw.commonUtil.service.BaseImpl;
import org.springframework.stereotype.Service;
import com.lyw.cloudRanking.service.CourseHeatDailyBo;

import java.util.Calendar;
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
     * @param period 周期类型，支持：day, week, month, quarter, year, 或自定义天数(如 7d, 30d)
     * @return 计算得到的开始日期
     */
    private Date calculateStartDate(String period) {
        if (period == null || period.trim().isEmpty()) {
            return getDefaultStartDate(); // 默认7天前
        }

        String normalizedPeriod = period.trim().toLowerCase();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        try {
            switch (normalizedPeriod) {
                case "today":
                case "day":
                case "1d":
                    // 今天开始时间（不减去天数）
                    break;

                case "yesterday":
                    // 昨天开始时间
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    break;

                case "week":
                case "7d":
                    // 一周前
                    calendar.add(Calendar.DAY_OF_MONTH, -7);
                    break;

                case "month":
                    // 一个月前
                    calendar.add(Calendar.MONTH, -1);
                    break;

                case "quarter":
                    // 一个季度前（3个月）
                    calendar.add(Calendar.MONTH, -3);
                    break;

                case "year":
                    // 一年前
                    calendar.add(Calendar.YEAR, -1);
                    break;

                case "month_start":
                    // 本月第一天
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    break;

                case "year_start":
                    // 本年第一天
                    calendar.set(Calendar.MONTH, Calendar.JANUARY);
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    break;

                default:
                    // 处理自定义天数格式，如 "30d", "90d"
                    if (normalizedPeriod.endsWith("d")) {
                        return handleCustomDays(normalizedPeriod);
                    } else {
                        // 未知周期类型，使用默认
                        return getDefaultStartDate();
                    }
            }

            return calendar.getTime();

        } catch (Exception e) {
            // 记录日志
            System.err.println("计算开始日期失败，使用默认值。period: " + period + ", error: " + e.getMessage());
            return getDefaultStartDate();
        }
    }

    /**
     * 处理自定义天数格式
     */
    private Date handleCustomDays(String period) {
        try {
            int days = Integer.parseInt(period.substring(0, period.length() - 1));
            if (days <= 0) {
                return getDefaultStartDate();
            }
            long timestamp = System.currentTimeMillis() - (long) days * 24 * 60 * 60 * 1000L;
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            return getDefaultStartDate();
        }
    }

    /**
     * 获取默认开始日期（7天前）
     */
    private Date getDefaultStartDate() {
        return new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L);
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