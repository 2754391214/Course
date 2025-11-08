package com.lyw.cloudMember.controller;

import com.lyw.cloudMember.dto.*;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.UserBo;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 用户基础表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 用户基础表")
@RestController
@RequestMapping("user")
public class UserController extends BaseController<UserDto> {

    private final UserBo service;

    @Override
    public UserBo getBaseService() {
        return service;
    }

    @PostMapping("/register")
    public CourseResponseWrapper register(@RequestBody UserRegisterDto dto){
        return service.register(dto);
    }
    @PostMapping("/login")
    public CourseResponseWrapper login(@RequestBody UserLoginDto dto){
        return service.login(dto);
    }
    @PutMapping("/password")
    public CourseResponseWrapper changePassword(@RequestBody ChangePasswordDto dto){
        return service.changePassword(dto);
    }

    @PostMapping(value = "/oauth2/login")
    public CourseResponseWrapper oauthLogin(@RequestBody SocialUserDto dto) throws Exception {
        return service.oauthLogin(dto);
    }
}