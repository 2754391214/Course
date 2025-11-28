package com.lyw.cloudRanking.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("cloudCourse")
@Component
public interface CourseFeignService {
    @GetMapping("/courses/{id}")
    CourseResponseWrapper searchDetail(@PathVariable("id") Long id);
}
