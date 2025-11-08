package com.lyw.cloudAuthServer.service;

import com.lyw.cloudAuthServer.dto.UserLoginDto;
import com.lyw.cloudAuthServer.dto.UserRegisterDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import javax.servlet.http.HttpServletRequest;

public interface LoginService {
    CourseResponseWrapper sendCode(String phone);

    CourseResponseWrapper register(UserRegisterDto dto);

    CourseResponseWrapper login(UserLoginDto dto);

    CourseResponseWrapper logout(HttpServletRequest request);

    CourseResponseWrapper validateToken(String token);

    CourseResponseWrapper getUserIdByToken(String token);
}
