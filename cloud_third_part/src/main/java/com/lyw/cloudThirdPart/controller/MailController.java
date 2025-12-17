package com.lyw.cloudThirdPart.controller;

import com.lyw.cloudThirdPart.aspect.ThirdPartyProtect;
import com.lyw.cloudThirdPart.dto.EmailDto;
import com.lyw.cloudThirdPart.service.MailService;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("mail")
public class MailController {
    @Resource
    private MailService mailService;
    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.EMAIL,
            count = 20,
            timeWindow = 300000,
            sensitivity = ThirdPartyProtect.CostSensitivity.MEDIUM
    )
    /**
     * 发送普通文本邮件
     */
    @PostMapping("/sendSimpleEmail")
    public CourseResponseWrapper sendSimpleMail(@RequestBody EmailDto dto) {
        return mailService.sendSimpleEmail(dto);
    }
    @ThirdPartyProtect(
            service = ThirdPartyProtect.ServiceType.EMAIL,
            count = 20,
            timeWindow = 300000,
            sensitivity = ThirdPartyProtect.CostSensitivity.MEDIUM
    )
    /**
     * 发送批量普通文本邮件
     */
    @PostMapping("/sendSimpleEmailList")
    public CourseResponseWrapper sendSimpleEmailList(@RequestBody List<EmailDto> dto) {
        return mailService.sendSimpleEmailList(dto);
    }
}
