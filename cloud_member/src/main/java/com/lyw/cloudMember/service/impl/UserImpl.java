package com.lyw.cloudMember.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyw.cloudMember.dto.ChangePasswordDto;
import com.lyw.cloudMember.dto.SocialUserDto;
import com.lyw.cloudMember.dto.UserLoginDto;
import com.lyw.cloudMember.dto.UserRegisterDto;
import com.lyw.cloudMember.mapper.UserAuthDao;
import com.lyw.cloudMember.mapper.UserDao;
import com.lyw.cloudMember.service.UserBo;
import com.lyw.cloudMember.vo.UserAuthVo;
import com.lyw.cloudMember.vo.UserVo;
import com.lyw.commonUtil.exception.UserPerceivableSpecificException;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.AES256;
import com.lyw.commonUtil.util.CurUserUtil;
import com.lyw.commonUtil.util.DateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户基础表 服务类
 * </p>
 *
 * @author lyw
 * @since 2025/11/06
 */
@Slf4j
@Service
public class UserImpl extends ServiceImpl<UserDao, UserVo> implements UserBo {

    @Resource
    private UserDao userDao;

    @Resource
    private UserAuthDao userAuthDao;

    @Override
    public CourseResponseWrapper register(UserRegisterDto dto) {
        try {
            // 1. 验证手机号是否已注册
            LambdaQueryWrapper<UserVo> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(UserVo::getPhone, dto.getPhone());
            UserVo existingUser = userDao.selectOne(phoneQuery);
            if (ObjectUtil.isNotEmpty(existingUser)) {
                return CourseResponseWrapper.getFailed("该手机号已注册");
            }

            // 2. 验证用户名是否已存在
            LambdaQueryWrapper<UserVo> usernameQuery = new LambdaQueryWrapper<>();
            usernameQuery.eq(UserVo::getUserName, dto.getUserName());
            if (ObjectUtil.isNotEmpty(userDao.selectOne(usernameQuery))) {
                return CourseResponseWrapper.getFailed("用户名已存在");
            }

            // 3. 创建用户基础信息
            UserVo user = new UserVo();
            user.setUserName(dto.getUserName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());
            user.setNickname(StringUtils.isNotEmpty(dto.getNickname()) ?
                    dto.getNickname() : dto.getUserName());
            user.setAvatar(dto.getAvatar());
            user.setStatus(1); // 正常状态
            user.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            user.setCruAndLuu(CurUserUtil.getUserId());

            int userResult = userDao.insert(user);
            if (userResult <= 0) {
                return CourseResponseWrapper.getFailed("用户注册失败");
            }

            // 4. 创建用户认证信息
            UserAuthVo userAuth = new UserAuthVo();
            userAuth.setUserId(user.getId());
            userAuth.setIdentityType("password"); // 密码登录方式
            userAuth.setIdentifier(dto.getUserName()); // 使用用户名作为标识
            userAuth.setCredential(AES256.encrypt(dto.getPassword())); // 加密密码
            userAuth.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
            userAuth.setCruAndLuu(CurUserUtil.getUserId());

            int authResult = userAuthDao.insert(userAuth);
            if (authResult <= 0) {
                // 回滚用户记录
                userDao.deleteById(user.getId());
                return CourseResponseWrapper.getFailed("用户认证信息创建失败");
            }
            //如果已经有绑定phone则添加电话加密码登录验证
            Long userAuthId = userAuth.getId();
            if (StringUtils.isNotEmpty(dto.getPhone())){
                userAuth.setId(null);
                userAuth.setIdentifier(dto.getPhone()); // 使用用户名作为标识
            }
            authResult = userAuthDao.insert(userAuth);
            if (authResult <= 0) {
                // 回滚用户记录
                userAuthDao.deleteById(userAuthId);
                userDao.deleteById(user.getId());
                return CourseResponseWrapper.getFailed("用户认证信息创建失败");
            }

            // 5. 返回注册成功信息
            Map<String, Object> result = new HashMap<>();
            result.put("userId", user.getId());
            result.put("username", user.getUserName());
            result.put("phone", user.getPhone());

            return CourseResponseWrapper.getSuccess("注册成功", result);

        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("系统异常，注册失败");
        }
    }

    @Override
    public CourseResponseWrapper login(UserLoginDto dto) {
        try {
            // 1. 根据用户名或手机号查询用户认证信息
            LambdaQueryWrapper<UserAuthVo> authQuery = new LambdaQueryWrapper<>();
            authQuery.eq(UserAuthVo::getIdentifier, dto.getUserName())
                    .or()
                    .eq(UserAuthVo::getIdentifier, dto.getPhone())
                    .eq(UserAuthVo::getIdentityType, "password");

            UserAuthVo userAuth = userAuthDao.selectOne(authQuery);
            if (ObjectUtil.isEmpty(userAuth)) {
                return CourseResponseWrapper.getFailed("用户不存在");
            }

            // 2. 验证密码
            if (!StringUtils.equals(AES256.encrypt(dto.getPassword(), AES256.getKey()), userAuth.getCredential())) {
                return CourseResponseWrapper.getFailed("密码错误");
            }

            // 3. 查询用户基础信息
            UserVo user = userDao.selectById(userAuth.getUserId());
            if (ObjectUtil.isEmpty(user)) {
                return CourseResponseWrapper.getFailed("用户信息不存在");
            }

            // 4. 检查用户状态
            if (user.getStatus() != 1) {
                return CourseResponseWrapper.getFailed("用户已被禁用");
            }

            // 5. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userDao.updateById(user);

            // 6. 返回用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUserName());
            userInfo.put("phone", user.getPhone());
            userInfo.put("email", user.getEmail());
            userInfo.put("nickname", user.getNickname());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("lastLoginTime", user.getLastLoginTime());

            return CourseResponseWrapper.getSuccess("登录成功", userInfo);

        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("系统异常，登录失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseResponseWrapper changePassword(ChangePasswordDto dto) {
        try {
            // 1. 查询用户认证信息
            LambdaQueryWrapper<UserAuthVo> authQuery = new LambdaQueryWrapper<UserAuthVo>()
                    .eq(UserAuthVo::getUserId, dto.getUserId())
                    .eq(UserAuthVo::getIdentityType, "password");

            List<UserAuthVo> userAuthList = userAuthDao.selectList(authQuery);
            if (CollectionUtil.isEmpty(userAuthList)) {
                return CourseResponseWrapper.getFailed("用户不存在");
            }
            // 2. 验证原密码
            if (!StringUtils.equals(AES256.encrypt(dto.getOldPassword(), AES256.getKey()), userAuthList.get(0).getCredential())) {
                return CourseResponseWrapper.getFailed("原密码错误");
            }
            for (UserAuthVo userAuth: userAuthList){
                // 3. 更新密码
                userAuth.setCredential(AES256.decrypt(dto.getNewPassword()));
                userAuth.setCruAndLuu(DateTimeUtils.getCurrentDateTime());

                int result = userAuthDao.updateById(userAuth);
                if (result <= 0) {
                    throw new UserPerceivableSpecificException("密码修改失败");
                }
            }
            return CourseResponseWrapper.getSuccess("密码修改成功");

        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            throw new UserPerceivableSpecificException("系统异常，密码修改失败");
        }
    }

    @Override
    public CourseResponseWrapper oauthLogin(SocialUserDto dto) {
        try {
            // 1. 根据社交平台唯一标识查询用户认证信息
            LambdaQueryWrapper<UserAuthVo> authQuery = new LambdaQueryWrapper<UserAuthVo>()
                    .eq(UserAuthVo::getIdentifier, dto.getSocialUid())
                    .eq(UserAuthVo::getIdentityType, dto.getSocialType());

            UserAuthVo userAuth = userAuthDao.selectOne(authQuery);

            if (ObjectUtil.isNotEmpty(userAuth)) {
                // 2. 已绑定社交账号，直接登录
                UserVo user = userDao.selectById(userAuth.getUserId());
                if (ObjectUtil.isEmpty(user)) {
                    return CourseResponseWrapper.getFailed("用户信息不存在");
                }

                if (user.getStatus() != 1) {
                    return CourseResponseWrapper.getFailed("用户已被禁用");
                }

                // 更新最后登录时间
                user.setLastLoginTime(LocalDateTime.now());
                userDao.updateById(user);

                // 返回用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUserName());
                userInfo.put("phone", user.getPhone());
                userInfo.put("email", user.getEmail());
                userInfo.put("nickname", user.getNickname());
                userInfo.put("avatar", user.getAvatar());
                userInfo.put("lastLoginTime", user.getLastLoginTime());

                return CourseResponseWrapper.getSuccess("登录成功", userInfo);

            } else {
                // 3. 新用户，自动注册
                return registerSocialUser(dto);
            }

        } catch (Exception e) {
            log.error("第三方登录失败: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("第三方登录失败");
        }
    }

    @Override
    public CourseResponseWrapper searchDetail(Long userId) {
        return CourseResponseWrapper.getSuccess(baseMapper.selectInfoById(userId));
    }

    /**
     * 注册社交用户
     */
    private CourseResponseWrapper registerSocialUser(SocialUserDto dto) {
        // 1. 创建用户基础信息
        UserVo user = new UserVo();
        user.setUserName(generateSocialUserName(dto.getSocialType()));
        user.setNickname(StringUtils.isNotEmpty(dto.getNickname()) ?
                dto.getNickname() : user.getUserName());
        user.setAvatar(dto.getAvatar());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        user.setCruAndLuu(CurUserUtil.getUserId());
        user.setLastLoginTime(LocalDateTime.now());

        int userResult = userDao.insert(user);
        if (userResult <= 0) {
            return CourseResponseWrapper.getFailed("社交用户注册失败");
        }

        // 2. 创建社交认证信息
        UserAuthVo userAuth = new UserAuthVo();
        userAuth.setUserId(user.getId());
        userAuth.setIdentityType(dto.getSocialType()); // 如: wechat, github, etc.
        userAuth.setIdentifier(dto.getSocialUid());
        userAuth.setCredential(""); // 社交登录不需要密码
        userAuth.setCrdAndLud(DateTimeUtils.getCurrentDateTime());
        userAuth.setCruAndLuu(CurUserUtil.getUserId());

        int authResult = userAuthDao.insert(userAuth);
        if (authResult <= 0) {
            // 回滚用户记录
            userDao.deleteById(user.getId());
            return CourseResponseWrapper.getFailed("社交认证信息创建失败");
        }

        // 3. 返回用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUserName());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("email", user.getEmail());
        userInfo.put("lastLoginTime", user.getLastLoginTime());

        return CourseResponseWrapper.getSuccess("登录成功", userInfo);
    }

    /**
     * 生成社交用户名
     */
    private String generateSocialUserName(String socialType) {
        return socialType.toLowerCase() + "_" + System.currentTimeMillis();
    }
}