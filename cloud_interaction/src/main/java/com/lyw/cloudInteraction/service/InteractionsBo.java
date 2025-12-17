package com.lyw.cloudInteraction.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface InteractionsBo {

    CourseResponseWrapper getAllStatus(String targetType, Long targetId, Long userId);
}