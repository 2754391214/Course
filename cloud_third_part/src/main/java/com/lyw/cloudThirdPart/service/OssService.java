package com.lyw.cloudThirdPart.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    CourseResponseWrapper policy();

    CourseResponseWrapper upload(MultipartFile file);
}
