package com.lyw.cloudThirdPart.controller;

import com.lyw.cloudThirdPart.aspect.ThirdPartyProtect;
import com.lyw.cloudThirdPart.service.SmsSendService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping( "sms")
public class SmsSendController {
    @Resource
    private SmsSendService smsSendService;
    /**
     * 发送短信
     */
    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.SMS,
            count = 5,
            timeWindow = 60000,
            sensitivity = ThirdPartyProtect.CostSensitivity.HIGH,
            enableContextCheck = true
    )
    @GetMapping("/sendCode")
    public CourseResponseWrapper sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        return smsSendService.sendCode(phone,code);
    }

}
