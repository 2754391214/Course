package com.lyw.cloudCourse.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudCourse.service.CourseCategoriesBo;
import com.lyw.cloudCourse.dto.CourseCategoriesDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 课程分类表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 课程分类表")
@RestController
@RequestMapping("courseCategories")
public class CourseCategoriesController extends BaseController<CourseCategoriesDto> {

    private final CourseCategoriesBo service;

    @Override
    public CourseCategoriesBo getBaseService() {
        return service;
    }

    /**
     * 获取分类下的课程
     */
    @GetMapping("/{categoryId}/courses")
    public CourseResponseWrapper getCategoryCourses(@PathVariable Long categoryId, CourseCategoriesDto dto) {
        return service.getCategoryCourses(categoryId, dto);
    }
}