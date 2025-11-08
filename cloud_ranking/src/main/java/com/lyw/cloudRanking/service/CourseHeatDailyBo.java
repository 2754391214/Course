package com.lyw.cloudRanking.service;

import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.lyw.cloudRanking.dto.CourseHeatDailyDto;
import com.lyw.commonUtil.service.BaseBo;

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
public interface CourseHeatDailyBo extends BaseBo<CourseHeatDailyVo,CourseHeatDailyDto> {
    List<CourseHeatDailyVo> getRecentHeatData(Long courseId, String period);
    List<CourseHeatDailyVo> getRankingSnapshot(String rankingCode, Date date);
}