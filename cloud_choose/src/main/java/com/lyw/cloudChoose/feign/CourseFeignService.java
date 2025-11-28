package com.lyw.cloudChoose.feign;

import com.lyw.cloudChoose.dto.CoursesDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("cloudCourse")
@Component
public interface CourseFeignService {
    @GetMapping("/courses/{id}")
    CourseResponseWrapper searchDetail(@PathVariable("id") Long id);

    // 新增：增加选课人数
    @PutMapping("/courses/{courseId}/increment-enrollment")
    CourseResponseWrapper incrementEnrollment(@PathVariable("courseId") Long courseId);

    // 新增：减少选课人数
    @PutMapping("/courses/{courseId}/decrement-enrollment")
    CourseResponseWrapper decrementEnrollment(@PathVariable("courseId") Long courseId);

    // 新增：获取课程当前状态
    @GetMapping("/courses/{courseId}/status")
    CourseResponseWrapper getCourseStatus(@PathVariable("courseId") Long courseId);

    // 新增：批量更新课程容量（用于抽签）
    @PutMapping("/courses/batch-update-capacity")
    CourseResponseWrapper batchUpdateCapacity(@RequestBody List<CoursesDto> updates);

    // 新增：检查课程时间是否冲突
    @GetMapping("/courseSchedules/{courseId}/checkTime-conflict")
    CourseResponseWrapper checkTimeConflict(@PathVariable("courseId") Long courseId,
                                            @RequestParam("studentId") Long studentId,
                                            @RequestParam("enrolledCourseIds") List<Long> enrolledCourseIds);
    @GetMapping("/courseSchedules/batchGetCourseSchedules")
    CourseResponseWrapper batchGetCourseSchedules(@RequestParam("courseIds") List<Long> courseIds);
}
