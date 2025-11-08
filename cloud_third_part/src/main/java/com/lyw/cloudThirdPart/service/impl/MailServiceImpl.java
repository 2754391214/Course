package com.lyw.cloudThirdPart.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudThirdPart.dto.EmailDto;
import com.lyw.cloudThirdPart.service.MailService;
import com.lyw.cloudThirdPart.utils.MailUtils;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class MailServiceImpl implements MailService {
    @Resource
    private MailUtils mailUtils;
    @Override
    public CourseResponseWrapper sendSimpleEmail(EmailDto dto) {
        return CourseResponseWrapper.getSuccess(
                mailUtils.sendSampleMail(dto.getEmail(),dto.getTitle(),dto.getContent()));
    }

    @Override
    public CourseResponseWrapper sendSimpleEmailList(List<EmailDto> dto) {
        if (CollectionUtil.isNotEmpty(dto)){
            dto.stream().forEach(item->sendSimpleEmail(item));
        }
        return CourseResponseWrapper.getSuccess();
    }
}
