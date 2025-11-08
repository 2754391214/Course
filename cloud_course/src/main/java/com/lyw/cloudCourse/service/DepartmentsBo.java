package com.lyw.cloudCourse.service;

import com.lyw.cloudCourse.vo.DepartmentsVo;
import com.lyw.cloudCourse.dto.DepartmentsDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 院系信息表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface DepartmentsBo extends BaseBo<DepartmentsVo,DepartmentsDto> {

    CourseResponseWrapper getDepartmentCourses(Long departmentId, String semester);
}