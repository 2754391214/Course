package com.lyw.cloudRanking.mapper;

import com.lyw.cloudRanking.vo.CourseHeatDailyVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 课程热度日快照表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/31
 */
@Mapper
public interface CourseHeatDailyDao extends BaseMapper<CourseHeatDailyVo> {

}
