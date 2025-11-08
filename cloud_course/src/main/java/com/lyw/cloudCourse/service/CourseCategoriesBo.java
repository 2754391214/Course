package com.lyw.cloudCourse.service;

import com.lyw.cloudCourse.vo.CourseCategoriesVo;
import com.lyw.cloudCourse.dto.CourseCategoriesDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 课程分类表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
public interface CourseCategoriesBo extends BaseBo<CourseCategoriesVo,CourseCategoriesDto> {

    CourseResponseWrapper getCategoryCourses(Long categoryId, CourseCategoriesDto dto);
}