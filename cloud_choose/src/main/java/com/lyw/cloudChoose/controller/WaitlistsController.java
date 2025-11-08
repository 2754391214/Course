package com.lyw.cloudChoose.controller;

import com.lyw.cloudChoose.dto.WaitlistsDto;
import com.lyw.cloudChoose.service.WaitlistsBo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 选课等待列表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/10/22
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 选课等待列表")
@RestController
@RequestMapping("Waitlists")
public class WaitlistsController {

    private final WaitlistsBo service;


    /**
     * 处理等待列表
     */
    @PostMapping("/courses/{courseId}/waitlist/process")
    public CourseResponseWrapper processWaitlist(@PathVariable Long courseId) {
        return service.processWaitlist(courseId);
    }

    /**
     * 加入等待列表
     */
    @PostMapping("/waitlists")
    public CourseResponseWrapper joinWaitlist(@RequestBody WaitlistsDto dto) {
        return service.joinWaitlist(dto);
    }

    /**
     * 退出等待列表
     */
    @DeleteMapping("/waitlists/{waitlistId}")
    public CourseResponseWrapper leaveWaitlist(@PathVariable Long waitlistId,
                                               @RequestParam WaitlistsDto dto) {
        return service.leaveWaitlist(waitlistId,dto);
    }

    /**
     * 获取学生的等待列表
     */
    @GetMapping("/waitlists/students/{studentId}")
    public CourseResponseWrapper getStudentWaitlists(@PathVariable Long studentId) {
        return service.getStudentWaitlists(studentId);
    }

    /**
     * 接受等待列表席位
     */
    @PostMapping("/waitlists/{waitlistId}/accept")
    public CourseResponseWrapper acceptWaitlistOffer(@PathVariable Long waitlistId) {
        return service.acceptWaitlistOffer(waitlistId);
    }
}