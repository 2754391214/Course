package com.lyw.cloudMember.controller;

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
@RequestMapping("TeacherProfile")
public class TeacherProfileController extends BaseController<TeacherProfileDto> {

    private final TeacherProfileBo service;

    @Override
    public TeacherProfileBo getBaseService() {
        return service;
    }
}