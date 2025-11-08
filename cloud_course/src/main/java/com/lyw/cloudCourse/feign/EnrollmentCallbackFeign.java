package com.lyw.cloudCourse.feign;

import com.lyw.cloudCourse.dto.TransactionCallbackDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cloudChoose")
public interface EnrollmentCallbackFeign {

    /**
     * 通知选课成功
     */
    @PostMapping("/callback/success")
    CourseResponseWrapper notifySuccess(@RequestBody TransactionCallbackDto callback);

    /**
     * 通知选课失败
     */
    @PostMapping("/callback/failure")
    CourseResponseWrapper notifyFailure(@RequestBody TransactionCallbackDto callback);
}