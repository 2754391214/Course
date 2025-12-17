package com.lyw.cloudCourse.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudCourse.service.CourseSchedulesBo;
import com.lyw.cloudCourse.dto.CourseSchedulesDto;
import com.lyw.commonUtil.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 课程时间安排表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 课程时间安排表")
@RestController
@RequestMapping("courseSchedules")
public class CourseSchedulesController extends BaseController<CourseSchedulesDto> {

    private final CourseSchedulesBo service;
    @Override
    public CourseSchedulesBo getBaseService() {
        return service;
    }

    /**
     * 批量获取课程课程时间安排
     * @param courseIds
     * @return
     */
    @GetMapping("/batchGetCourseSchedules")
    CourseResponseWrapper batchGetCourseSchedules(@RequestParam("courseIds") List<Long> courseIds){
        return service.batchGetCourseSchedules(courseIds);
    }
}