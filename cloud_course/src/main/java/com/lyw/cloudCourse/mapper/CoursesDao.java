package com.lyw.cloudCourse.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 课程基础信息表 Mapper 接口
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@Mapper
public interface CoursesDao extends BaseMapper<CoursesVo> {

    List<CoursesVo> selectPopularCourses(@Param("limit") Integer limit,@Param("semester") String semester,@Param("departmentId") Long departmentId);

    List<CoursesVo> selectCategoryCourses(Page<CoursesVo> page,@Param("ew") QueryWrapper<CoursesVo> eq);
}
