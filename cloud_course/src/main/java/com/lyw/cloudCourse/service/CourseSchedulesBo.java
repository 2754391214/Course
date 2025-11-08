package com.lyw.cloudCourse.service;

import com.lyw.cloudCourse.vo.CourseSchedulesVo;
import com.lyw.cloudCourse.dto.CourseSchedulesDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

import java.util.List;

/**
 * <p>
 * 课程时间安排表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface CourseSchedulesBo extends BaseBo<CourseSchedulesVo,CourseSchedulesDto> {

    CourseResponseWrapper checkTimeConflict(Long courseId, Long studentId, List<Long> enrolledCourseIds);
}