package com.lyw.cloudChoose.controller;


import com.lyw.cloudChoose.dto.EnrollmentBlacklistDto;
import com.lyw.cloudChoose.service.EnrollmentBlacklistBo;
import com.lyw.commonUtil.controller.BaseController;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 选课黑名单表，用于管理学生选课限制，支持全局黑名单和课程特定黑名单，保障选课系统的公平性和规范性 前端控制器
 * </p>
 *
 * @author lyw
 * @since 2025/10/24
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 选课黑名单表")
@RestController
@RequestMapping("enrollmentBlacklist")
public class EnrollmentBlacklistController extends BaseController<EnrollmentBlacklistDto> {
    private final EnrollmentBlacklistBo service;

    @Override
    public EnrollmentBlacklistBo getBaseService() {
        return service;
    }
}

