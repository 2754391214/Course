package com.lyw.cloudMember.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.StudentProfileBo;
import com.lyw.cloudMember.dto.StudentProfileDto;
import com.lyw.commonUtil.controller.BaseController;

import java.util.List;

/**
 * <p>
 * 学生信息表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 学生信息表")
@RestController
@RequestMapping("studentProfile")
public class StudentProfileController extends BaseController<StudentProfileDto> {

    private final StudentProfileBo service;

    @Override
    public StudentProfileBo getBaseService() {
        return service;
    }
    @PutMapping("/bindUser/{userId}")
    public CourseResponseWrapper bindUser(@PathVariable Long userId, @RequestParam Long studentId) {
        return service.bindUser(userId, studentId);
    }
    @PutMapping("/unbindUser/{userId}")
    public CourseResponseWrapper unbindUser(@PathVariable Long userId, @RequestParam Long studentId) {
        return service.unbindUser(userId, studentId);
    }
    @GetMapping("/batchGet")
    public CourseResponseWrapper searchBatchByIds(@RequestParam("userIds") List<Long> userIds) {
        return service.searchBatchByIds(userIds);
    }
}