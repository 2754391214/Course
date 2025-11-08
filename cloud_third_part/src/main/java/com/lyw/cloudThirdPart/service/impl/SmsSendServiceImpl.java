package com.lyw.cloudThirdPart.service.impl;

import com.lyw.cloudThirdPart.utils.SmsUtils;
import com.lyw.cloudThirdPart.service.SmsSendService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class SmsSendServiceImpl implements SmsSendService {
    @Resource
    private SmsUtils smsUtils;

    @Override
    public CourseResponseWrapper sendCode(String phone, String code) {
        //发送验证码
        smsUtils.sendSmsCode(phone,code);
        return CourseResponseWrapper.getSuccess();
    }
}
