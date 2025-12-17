package com.lyw.cloudMember.controller;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.UserPreferenceBo;
import com.lyw.cloudMember.dto.UserPreferenceDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 用户偏好表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 用户偏好表")
@RestController
@RequestMapping("userPreference")
public class UserPreferenceController extends BaseController<UserPreferenceDto> {

    private final UserPreferenceBo service;

    @Override
    public UserPreferenceBo getBaseService() {
        return service;
    }
    @GetMapping("/user/{userId}")
    public CourseResponseWrapper findDetailByUser(@PathVariable Long userId) {
        return service.findDetailByUser(userId);
    }
}