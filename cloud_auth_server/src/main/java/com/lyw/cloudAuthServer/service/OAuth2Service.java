package com.lyw.cloudAuthServer.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface OAuth2Service {
    CourseResponseWrapper gitee(String code) throws Exception;

    CourseResponseWrapper weibo(String code) throws Exception;
}
