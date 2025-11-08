package com.lyw.cloudAuthServer.feign;

import com.lyw.cloudAuthServer.dto.SocialUserDto;
import com.lyw.cloudAuthServer.dto.UserLoginDto;
import com.lyw.cloudAuthServer.dto.UserRegisterDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient("cloudMember")
public interface MemberFeignService {
    @PostMapping("/user/regist")
    CourseResponseWrapper register(@RequestBody UserRegisterDto dto);
    @PostMapping("/user/login")
    CourseResponseWrapper login(@RequestBody UserLoginDto dto);
    @PostMapping(value = "/user/oauth2/login")
    CourseResponseWrapper oauthLogin(@RequestBody SocialUserDto dto) throws Exception;
}
