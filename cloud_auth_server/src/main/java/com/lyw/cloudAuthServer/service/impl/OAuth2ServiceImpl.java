package com.lyw.cloudAuthServer.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudAuthServer.dto.SocialUserDto;
import com.lyw.cloudAuthServer.factory.OAuth2ServiceFactory;
import com.lyw.cloudAuthServer.feign.MemberFeignService;
import com.lyw.cloudAuthServer.service.OAuth2Service;
import com.lyw.cloudAuthServer.utils.JwtTokenUtil;
import com.lyw.commonUtil.constant.AuthServerConstant;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class OAuth2ServiceImpl implements OAuth2Service {

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private OAuth2ServiceFactory oAuth2ServiceFactory;

    /**
     * 处理Gitee OAuth2登录
     */
    @Override
    public CourseResponseWrapper gitee(String code) {
        log.info("Gitee OAuth2登录，授权码: {}", code);
        return handleOAuth2Login("gitee", code);
    }

    /**
     * 处理微博OAuth2登录
     */
    @Override
    public CourseResponseWrapper weibo(String code) {
        log.info("微博 OAuth2登录，授权码: {}", code);
        return handleOAuth2Login("weibo", code);
    }

    /**
     * 统一的OAuth2登录处理方法
     */
    private CourseResponseWrapper handleOAuth2Login(String socialType, String code) {
        try {
            // 1. 验证社交类型是否支持
            if (!oAuth2ServiceFactory.isSocialTypeSupported(socialType)) {
                log.error("不支持的社交登录类型: {}", socialType);
                return CourseResponseWrapper.getFailed("不支持的登录方式");
            }

            // 2. 获取access_token
            CourseResponseWrapper tokenResult = oAuth2ServiceFactory.getAccessToken(socialType, code);
            if (!tokenResult.isSuccess()) {
                return tokenResult;
            }

            SocialUserDto socialUser = (SocialUserDto) tokenResult.getData();

            // 3. 获取用户信息
            CourseResponseWrapper userInfoResult = oAuth2ServiceFactory.getUserInfo(socialType, socialUser);
            if (!userInfoResult.isSuccess()) {
                return userInfoResult;
            }

            socialUser = (SocialUserDto) userInfoResult.getData();

            // 4. 处理登录
            return handleSocialLogin(socialUser);

        } catch (Exception e) {
            log.error("{} 登录处理异常: {}", socialType, e.getMessage(), e);
            return CourseResponseWrapper.getFailed(socialType + "登录处理异常");
        }
    }

    /**
     * 处理社交登录通用逻辑
     */
    private CourseResponseWrapper handleSocialLogin(SocialUserDto socialUser) {
        try {
            log.info("处理社交登录，社交类型: {}, 用户: {}", socialUser.getSocialType(), socialUser.getName());

            // 调用会员服务进行社交登录/注册
            CourseResponseWrapper responseWrapper = memberFeignService.oauthLogin(socialUser);

            // 处理登录结果
            return handleLoginResult(responseWrapper);

        } catch (Exception e) {
            log.error("社交登录处理失败: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("社交登录处理失败");
        }
    }

    /**
     * 处理登录结果，生成token等
     */
    private CourseResponseWrapper handleLoginResult(CourseResponseWrapper loginResponse) {
        if (!loginResponse.isSuccess()) {
            return loginResponse;
        }

        Map<String, Object> data = (Map<String, Object>) loginResponse.getData();
        if (CollectionUtil.isEmpty(data)) {
            return CourseResponseWrapper.getFailed("登录成功但用户信息为空");
        }

        try {
            String userId = data.get("id").toString();
            String username = data.get("username").toString();

            // 生成JWT token
            String token = jwtTokenUtil.generateToken(userId, username);

            // 存储token到Redis（单点登录）
            storeTokenForSingleSignOn(token, userId);

            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", data);
            result.put("expire", AuthServerConstant.LOGIN_TOKEN_EXPIRE);

            log.info("社交登录成功，用户ID: {}, 用户名: {}", userId, username);
            return CourseResponseWrapper.getSuccess(result);

        } catch (Exception e) {
            log.error("处理登录结果时发生错误: {}", e.getMessage(), e);
            return CourseResponseWrapper.getFailed("系统异常，登录处理失败");
        }
    }

    /**
     * 存储token到Redis（单点登录）
     */
    private void storeTokenForSingleSignOn(String token, String userId) {
        String tokenKey = AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + token;
        String userKey = AuthServerConstant.LOGIN_USER_ID_PREFIX + userId;

        // 删除该用户之前的token（单点登录）
        String oldToken = redisUtils.get(userKey);
        if (StringUtils.isNotEmpty(oldToken)) {
            redisUtils.delete(AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + oldToken);
        }

        // 存储新的token
        redisUtils.set(tokenKey, userId, Duration.ofSeconds(AuthServerConstant.LOGIN_TOKEN_EXPIRE));
        redisUtils.set(userKey, token, Duration.ofSeconds(AuthServerConstant.LOGIN_TOKEN_EXPIRE));
    }

    /**
     * 获取支持的社交登录类型
     */
    public String getSupportedSocialTypes() {
        return oAuth2ServiceFactory.getSupportedSocialTypes();
    }
}