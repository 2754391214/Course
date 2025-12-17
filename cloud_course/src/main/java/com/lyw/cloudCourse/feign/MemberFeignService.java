package com.lyw.cloudCourse.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("cloudMember")
@Component
public interface MemberFeignService {
    @GetMapping("/teacherProfile/{id}")
    CourseResponseWrapper searchDetail(@PathVariable("id") Long id);
}
