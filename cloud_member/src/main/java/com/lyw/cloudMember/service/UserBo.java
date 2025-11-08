package com.lyw.cloudMember.service;

import com.lyw.cloudMember.dto.*;
import com.lyw.cloudMember.vo.UserVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.service.BaseBo;

/**
 * <p>
 * 用户基础表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
public interface UserBo extends BaseBo<UserVo,UserDto> {

    CourseResponseWrapper register(UserRegisterDto dto);

    CourseResponseWrapper login(UserLoginDto dto);

    CourseResponseWrapper changePassword(ChangePasswordDto dto);

    CourseResponseWrapper oauthLogin(SocialUserDto dto);
}