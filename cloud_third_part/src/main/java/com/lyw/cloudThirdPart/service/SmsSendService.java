package com.lyw.cloudThirdPart.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface SmsSendService {
    CourseResponseWrapper sendCode(String phone, String code);
}
