package com.lyw.cloudCourse.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("cloudInteraction")
@Component
public interface InteractionFeignService {
    @GetMapping("/interaction/status")
    CourseResponseWrapper getFavoriteStatus(@RequestParam("targetType") String targetType, @RequestParam("targetId") Long targetId, @RequestParam("userId") Long userId);
}
