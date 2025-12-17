package com.lyw.cloudRecommend.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("cloudRanking")
@Component
public interface RankingFeignService {
    @GetMapping("/ranking/course")
    CourseResponseWrapper getCourseRanking(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size
    );
}
