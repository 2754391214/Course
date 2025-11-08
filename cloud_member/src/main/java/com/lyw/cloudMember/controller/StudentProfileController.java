package com.lyw.cloudMember.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.StudentProfileBo;
import com.lyw.cloudMember.dto.StudentProfileDto;
import com.lyw.commonUtil.controller.BaseController;

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
@RequestMapping("StudentProfile")
public class StudentProfileController extends BaseController<StudentProfileDto> {

    private final StudentProfileBo service;

    @Override
    public StudentProfileBo getBaseService() {
        return service;
    }
}