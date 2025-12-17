package com.lyw.cloudCourse.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudCourse.service.DepartmentsBo;
import com.lyw.cloudCourse.dto.DepartmentsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 院系信息表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 院系信息表")
@RestController
@RequestMapping("departments")
public class DepartmentsController extends BaseController<DepartmentsDto> {

    private final DepartmentsBo service;

    @Override
    public DepartmentsBo getBaseService() {
        return service;
    }

    /**
     * 获取院系的课程
     */
    @GetMapping("/{departmentId}/courses")
    public CourseResponseWrapper getDepartmentCourses(@PathVariable Long departmentId, @RequestParam(required = false) String semester) {
        return service.getDepartmentCourses(departmentId, semester);
    }
}