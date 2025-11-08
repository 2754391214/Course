package com.lyw.cloudAuthServer.controller;

import com.lyw.cloudAuthServer.dto.UserLoginDto;
import com.lyw.cloudAuthServer.dto.UserRegisterDto;
import com.lyw.cloudAuthServer.service.LoginService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    @GetMapping("/sms/sendCode")
    public CourseResponseWrapper sendCode(@RequestParam("phone") String phone) {
        return loginService.sendCode(phone);
    }

    @PostMapping(value = "/register")
    public CourseResponseWrapper register(@RequestBody UserRegisterDto dto) {
        return loginService.register(dto);
    }

    @PostMapping(value = "/login")
    public CourseResponseWrapper login(@RequestBody UserLoginDto vo) {
        return loginService.login(vo);
    }

    @PostMapping(value = "/logout")
    public CourseResponseWrapper logout(HttpServletRequest request) {
        return loginService.logout(request);
    }

    @GetMapping(value = "/validateToken")
    public CourseResponseWrapper validateToken(String token) {
        return loginService.validateToken(token);
    }

    @GetMapping(value = "/getUserIdByToken")
    public CourseResponseWrapper getUserIdByToken(String token) {
        return loginService.getUserIdByToken(token);
    }

}