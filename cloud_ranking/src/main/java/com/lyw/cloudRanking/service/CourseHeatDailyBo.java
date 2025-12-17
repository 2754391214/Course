package com.lyw.cloudRanking.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudRanking.vo.CourseHeatDailyVo;

import java.util.List;

/**
 * <p>
 * 课程热度日快照表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
public interface CourseHeatDailyBo extends IService<CourseHeatDailyVo> {
    List<CourseHeatDailyVo> getRecentHeatData(Long courseId, String period);
}