package com.lyw.cloudChoose.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudChoose.service.EnrollmentsBo;
import com.lyw.cloudChoose.dto.EnrollmentsDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 选课表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 选课表")
@RestController
@RequestMapping("Enrollments")
public class EnrollmentsController {
    private final EnrollmentsBo service;


    /**
     * 学生选课
     */
    @PostMapping("/enroll")
    public CourseResponseWrapper enroll(@RequestBody EnrollmentsDto dto) {
        return service.enroll(dto);
    }

    /**
     * 学生退课
     */
    @DeleteMapping("/{enrollmentId}")
    public CourseResponseWrapper drop(@PathVariable Long enrollmentId,
                                      @RequestBody EnrollmentsDto dto) {
        return service.drop(enrollmentId,dto);
    }

    /**
     * 获取学生的选课列表
     */
    @GetMapping("/students/{studentId}")
    public CourseResponseWrapper getStudentEnrollments(
            @PathVariable Long studentId,
            @RequestParam(required = false) EnrollmentsDto dto) {
        return service.getStudentEnrollments(studentId,dto);
    }

    /**
     * 获取学生课表
     */
    @GetMapping("/students/{studentId}/timetable")
    public CourseResponseWrapper getStudentTimetable(
            @PathVariable Long studentId,
            @RequestParam(required = false) EnrollmentsDto dto) {
        return service.getStudentTimetable(studentId,dto);
    }

    /**
     * 获取课程的选课学生列表
     */
    @GetMapping("/courses/{courseId}")
    public CourseResponseWrapper getCourseEnrollments(
            @PathVariable Long courseId,
            @RequestParam(required = false) EnrollmentsDto dto) {
        return service.getCourseEnrollments(courseId,dto);
    }
}