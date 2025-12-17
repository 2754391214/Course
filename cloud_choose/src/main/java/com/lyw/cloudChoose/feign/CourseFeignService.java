package com.lyw.cloudChoose.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("cloudCourse")
@Component
public interface CourseFeignService {
    @GetMapping("/courses/{id}")
    CourseResponseWrapper searchDetail(@PathVariable("id") Long id);
    @GetMapping("/courseSchedules/batchGetCourseSchedules")
    CourseResponseWrapper batchGetCourseSchedules(@RequestParam("courseIds") List<Long> courseIds);

    @GetMapping("/courses/batchGet")
    CourseResponseWrapper searchBatchByIds(@RequestParam("courseIds") List<Long> courseIds);

}
