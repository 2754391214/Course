package com.lyw.cloudMember.controller;

import com.lyw.cloudMember.dto.ThirdPartyBindDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.lyw.cloudMember.service.UserAuthBo;
import com.lyw.cloudMember.dto.UserAuthDto;
import com.lyw.commonUtil.controller.BaseController;

/**
 * <p>
 * 用户认证表 controller
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Api(tags = "REST - 用户认证表")
@RestController
@RequestMapping("userAuth")
public class UserAuthController extends BaseController<UserAuthDto> {

    private final UserAuthBo service;

    @Override
    public UserAuthBo getBaseService() {
        return service;
    }

    @PostMapping("/bind-third-party")
    public CourseResponseWrapper bindThirdPartyAccount(@RequestBody ThirdPartyBindDto dto){
        return null;
    }

    @DeleteMapping("/bind-third-party/{platform}")
    public CourseResponseWrapper unbindThirdPartyAccount(@PathVariable String platform){
        return null;
    }
}