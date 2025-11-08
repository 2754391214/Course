package com.lyw.cloudCourse.mapper;

import com.lyw.cloudCourse.vo.CourseSchedulesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 课程时间安排表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Mapper
public interface CourseSchedulesDao extends BaseMapper<CourseSchedulesVo> {

}
