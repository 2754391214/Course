package com.lyw.cloudCourse.service;

import com.lyw.cloudCourse.dto.CoursesDto;
import com.lyw.cloudCourse.vo.CoursesVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

import java.util.List;

/**
 * <p>
 * 课程基础信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface CoursesBo extends BaseBo<CoursesVo,CoursesDto> {
    CourseResponseWrapper findById(Long courseId);

    CourseResponseWrapper getPopularCourses(Integer limit, String semester);

    CourseResponseWrapper getTeacherCourses(Long teacherId, String semester);

    CourseResponseWrapper incrementEnrollment(Long courseId);

    CourseResponseWrapper decrementEnrollment(Long courseId);

    CourseResponseWrapper batchUpdateCapacity(List<CoursesDto> updates);

}