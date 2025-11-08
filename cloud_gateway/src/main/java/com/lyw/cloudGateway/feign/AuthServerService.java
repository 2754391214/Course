package com.lyw.cloudGateway.feign;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import dto.UserLoginDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient("cloudAuthServer")
public interface AuthServerService {
    @GetMapping(value = "/validateToken")
    CourseResponseWrapper validateToken(String token);

    @PostMapping(value = "/login")
    CourseResponseWrapper login(@RequestBody UserLoginDto vo);

    @GetMapping(value = "/getUserIdByToken")
    CourseResponseWrapper getUserIdByToken(String token);
}
