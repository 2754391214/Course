package com.lyw.cloudCourse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.cloudCourse.vo.CourseSchedulesVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    Integer checkTimeConflictInDB(@Param("courseId") Long courseId, @Param("enrolledCourseIds") List<Long> enrolledCourseIds);
}
