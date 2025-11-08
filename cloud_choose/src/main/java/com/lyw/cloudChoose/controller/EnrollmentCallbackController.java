package com.lyw.cloudChoose.controller;

import com.lyw.cloudChoose.dto.TransactionCallbackDto;
import com.lyw.cloudChoose.service.EnrollmentBlacklistBo;
import com.lyw.cloudChoose.service.EnrollmentCallBackBo;
import com.lyw.cloudChoose.service.EnrollmentTransationBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 选课退课的回调
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@RestController
@RequestMapping("callback")
@Slf4j
public class EnrollmentCallbackController {
    @Resource
    private EnrollmentCallBackBo service;

    @PostMapping("/success")
    public CourseResponseWrapper handleSuccess(@RequestBody TransactionCallbackDto callback) {
        return service.handleSuccess(callback);
    }

    @PostMapping("/failure")
    public CourseResponseWrapper handleFailure(@RequestBody TransactionCallbackDto callback) {
        return service.handleFailure(callback);
    }
}