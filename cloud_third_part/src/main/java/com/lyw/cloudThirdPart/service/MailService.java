package com.lyw.cloudThirdPart.service;

import com.lyw.cloudThirdPart.dto.EmailDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

import java.util.List;

public interface MailService {
    CourseResponseWrapper sendSimpleEmail(EmailDto dto);

    CourseResponseWrapper sendSimpleEmailList(List<EmailDto> dto);
}
