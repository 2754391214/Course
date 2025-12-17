package com.lyw.cloudMember.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.TeacherProfileBo;
import com.lyw.cloudMember.dto.TeacherProfileDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 教师信息表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 教师信息表")
@RestController
@RequestMapping("teacherProfile")
public class TeacherProfileController extends BaseController<TeacherProfileDto> {

    private final TeacherProfileBo service;

    @Override
    public TeacherProfileBo getBaseService() {
        return service;
    }

    @PutMapping("/bindUser/{userId}")
    public CourseResponseWrapper bindUser(@PathVariable Long userId, @RequestParam Long teacherId) {
        return service.bindUser(userId, teacherId);
    }
    @PutMapping("/unbindUser/{userId}")
    public CourseResponseWrapper unbindUser(@PathVariable Long userId, @RequestParam Long teacherId) {
        return service.unbindUser(userId, teacherId);
    }
}