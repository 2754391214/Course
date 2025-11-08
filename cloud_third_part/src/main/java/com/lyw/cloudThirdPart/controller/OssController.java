package com.lyw.cloudThirdPart.controller;

import com.lyw.cloudThirdPart.aspect.ThirdPartyProtect;
import com.lyw.cloudThirdPart.service.OssService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@Slf4j
@RestController("oss")
public class OssController {

    @Resource
    private OssService ossService;
    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.OSS,
            count = 50,
            timeWindow = 60000,
            sensitivity = ThirdPartyProtect.CostSensitivity.LOW
    )
    @RequestMapping("/oss/policy")
    public CourseResponseWrapper policy() {
        return ossService.policy();
    }

    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.OSS,
            count = 50,
            timeWindow = 60000,
            sensitivity = ThirdPartyProtect.CostSensitivity.LOW
    )
    @PostMapping("/oss/upload")
    public CourseResponseWrapper upload(MultipartFile file) {
        return ossService.upload(file);
    }
}
