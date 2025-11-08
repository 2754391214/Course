package com.lyw.cloudThirdPart.controller;

import com.lyw.cloudThirdPart.aspect.ThirdPartyProtect;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "ai")
public class AiController {
    /**
     * 提供给别的服务进行调用
     */
    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.AI_SERVICE,
            count = 10,
            timeWindow = 600000,
            sensitivity = ThirdPartyProtect.CostSensitivity.HIGH,
            coolDown = 1800
    )
    @GetMapping("/ai")
    public CourseResponseWrapper ai() {
        return CourseResponseWrapper.getSuccess();
    }
}
