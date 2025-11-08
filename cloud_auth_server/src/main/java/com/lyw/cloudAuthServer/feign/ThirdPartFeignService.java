package com.lyw.cloudAuthServer.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("cloudThirdPart")
public interface ThirdPartFeignService {
    @GetMapping("/sms/sendCode")
    CourseResponseWrapper sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
