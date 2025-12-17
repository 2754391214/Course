package com.lyw.cloudChoose.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("cloudMember")
@Component
public interface MemberFeignService {
    @GetMapping("/studentProfile/batchGet")
    CourseResponseWrapper searchBatchByIds(@RequestParam("userIds") List<Long> userIds);

}
