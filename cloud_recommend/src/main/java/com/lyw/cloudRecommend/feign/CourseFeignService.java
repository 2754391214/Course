package com.lyw.cloudRecommend.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient("cloudCourse")
@Component
public interface CourseFeignService {
    @GetMapping("/courses/batchGet")
    CourseResponseWrapper searchBatchByIds(@RequestParam("courseIds") List<Long> courseIds);

}
