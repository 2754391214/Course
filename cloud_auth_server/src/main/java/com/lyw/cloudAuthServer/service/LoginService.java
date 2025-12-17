package com.lyw.cloudAuthServer.service;

import com.lyw.cloudAuthServer.dto.UserLoginDto;
import com.lyw.cloudAuthServer.dto.UserRegisterDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface LoginService {
    CourseResponseWrapper sendCode(String phone);

    CourseResponseWrapper register(UserRegisterDto dto);

    CourseResponseWrapper login(UserLoginDto dto);

    CourseResponseWrapper logout(String token);

    CourseResponseWrapper validateToken(String token);

    CourseResponseWrapper getUserIdByToken(String token);
}
