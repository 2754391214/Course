package com.lyw.cloudMember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyw.cloudMember.dto.ChangePasswordDto;
import com.lyw.cloudMember.dto.SocialUserDto;
import com.lyw.cloudMember.dto.UserLoginDto;
import com.lyw.cloudMember.dto.UserRegisterDto;
import com.lyw.cloudMember.vo.UserVo;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;

/**
 * <p>
 * 用户基础表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
public interface UserBo extends IService<UserVo> {

    CourseResponseWrapper register(UserRegisterDto dto);

    CourseResponseWrapper login(UserLoginDto dto);

    CourseResponseWrapper changePassword(ChangePasswordDto dto);

    CourseResponseWrapper oauthLogin(SocialUserDto dto);

    CourseResponseWrapper searchDetail(Long userId);
}