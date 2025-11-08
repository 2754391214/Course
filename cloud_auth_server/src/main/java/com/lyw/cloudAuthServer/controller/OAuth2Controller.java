package com.lyw.cloudAuthServer.controller;

import com.lyw.cloudAuthServer.service.OAuth2Service;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;


@Slf4j
@Controller
public class OAuth2Controller {

    @Resource
    private OAuth2Service oAuth2Service;

    @GetMapping("/{socialType}/success")
    public CourseResponseWrapper oauth2Callback(@PathVariable String socialType,
                                                @RequestParam String code) {
        try {
            log.info("OAuth2回调，社交类型: {}, 授权码: {}", socialType, code);

            switch (socialType.toLowerCase()) {
                case "gitee":
                    return oAuth2Service.gitee(code);
                case "weibo":
                    return oAuth2Service.weibo(code);
                default:
                    return CourseResponseWrapper.getFailed("不支持的社交登录类型: " + socialType);
            }
        } catch (Exception e) {
            log.error("OAuth2回调处理异常: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("登录处理异常");
        }
    }

}
