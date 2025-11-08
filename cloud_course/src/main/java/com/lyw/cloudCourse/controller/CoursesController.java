package com.lyw.cloudCourse.controller;

import com.lyw.cloudCourse.dto.CourseSchedulesDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudCourse.service.CoursesBo;
import com.lyw.cloudCourse.dto.CoursesDto;
import com.lyw.commonUtil.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 课程基础信息表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 课程基础信息表")
@RestController
@RequestMapping("courses")
public class CoursesController extends BaseController<CoursesDto> {

    private final CoursesBo service;

    @Override
    public CoursesBo getBaseService() {
        return service;
    }

    /**
     * 获取热门课程
     */
    @GetMapping("/popular")
    public CourseResponseWrapper getPopularCourses(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String semester) {
        return service.getPopularCourses(limit, semester);
    }

    /**
     * 获取教师授课的课程
     */
    @GetMapping("/teachers/{teacherId}")
    public CourseResponseWrapper getTeacherCourses(
            @PathVariable Long teacherId,
            @RequestParam(required = false) String semester) {
        return service.getTeacherCourses(teacherId, semester);
    }


    /**
     * 增加课程选课人数
     */
    @PutMapping("/{courseId}/increment-enrollment")
    public CourseResponseWrapper incrementEnrollment(@PathVariable Long courseId) {
        return service.incrementEnrollment(courseId);
    }

    /**
     * 减少课程选课人数
     */
    @PutMapping("/{courseId}/decrement-enrollment")
    public CourseResponseWrapper decrementEnrollment(@PathVariable Long courseId) {
        return service.decrementEnrollment(courseId);
    }

    /**
     * 批量更新课程容量
     */
    @PutMapping("/batch-update-capacity")
    public CourseResponseWrapper batchUpdateCapacity(@RequestBody List<CoursesDto> updates) {
        return service.batchUpdateCapacity(updates);
    }
}