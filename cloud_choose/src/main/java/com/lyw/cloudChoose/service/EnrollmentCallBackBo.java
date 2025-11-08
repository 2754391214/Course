package com.lyw.cloudChoose.service;

import com.lyw.cloudChoose.dto.TransactionCallbackDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

public interface EnrollmentCallBackBo {
    CourseResponseWrapper handleSuccess(TransactionCallbackDto callback);

    CourseResponseWrapper handleFailure(TransactionCallbackDto callback);
}
