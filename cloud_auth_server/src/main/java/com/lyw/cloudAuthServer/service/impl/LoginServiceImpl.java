package com.lyw.cloudAuthServer.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lyw.cloudAuthServer.dto.UserLoginDto;
import com.lyw.cloudAuthServer.dto.UserRegisterDto;
import com.lyw.cloudAuthServer.feign.MemberFeignService;
import com.lyw.cloudAuthServer.feign.ThirdPartFeignService;
import com.lyw.cloudAuthServer.service.LoginService;
import com.lyw.cloudAuthServer.utils.JwtTokenUtil;
import com.lyw.cloudAuthServer.utils.ValidateCodeUtils;
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
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private ThirdPartFeignService thirdPartFeignService;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private MemberFeignService memberFeignService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public CourseResponseWrapper sendCode(String phone) {
        String redisCode = redisUtils.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotEmpty(redisCode)) {
            long preSendTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - preSendTime < 60000) {
                return CourseResponseWrapper.getFailed("验证码获取频率太高，请稍后再试");
            }
        }
        String code = String.valueOf(ValidateCodeUtils.generateValidateCode(4));
        String code1 = code + "_" + System.currentTimeMillis();
        redisUtils.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code1, Duration.ofMinutes(10));
        log.info("{}验证码：{}",phone,code);
        thirdPartFeignService.sendCode(phone, code);
        return CourseResponseWrapper.getSuccess();
    }

    @Override
    public CourseResponseWrapper register(UserRegisterDto dto) {
        //1、效验验证码
        String code = dto.getVerificationCode();

        //获取存入Redis里的验证码
        String redisCode = redisUtils.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + dto.getPhone());
        if (StringUtils.isNotEmpty(redisCode)) {
            //截取字符串
            if (code.equals(redisCode.split("_")[0])) {
                //删除验证码;令牌机制
                redisUtils.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + dto.getPhone());
                //验证码通过，真正注册，调用远程服务进行注册
                return memberFeignService.register(dto);
            }
        }
        return CourseResponseWrapper.getFailed("验证码错误");
    }

    @Override
    public CourseResponseWrapper login(UserLoginDto dto) {
        // 远程登录验证
        CourseResponseWrapper loginResult = memberFeignService.login(dto);

        if (loginResult.isSuccess()) {
            // 登录成功，生成token并存储到Redis
            Map<String, Object> data = (Map<String, Object>) loginResult.getData();
            if (CollectionUtil.isNotEmpty(data)) {
                String userId = data.get("id").toString();
                String username = data.get("username").toString();

                // 生成JWT token
                String token = jwtTokenUtil.generateToken(userId, username);

                // 存储到Redis，实现单点登录
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

                // 返回token给前端
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("userInfo", data);
                result.put("expire", AuthServerConstant.LOGIN_TOKEN_EXPIRE);

                return CourseResponseWrapper.getSuccess(result);
            }
        }

        return loginResult;
    }

    /**
     * 验证token有效性
     */
    public CourseResponseWrapper validateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return CourseResponseWrapper.getFailed("token无效");
        }

        // 检查Redis中是否存在该token
        String tokenKey = AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + token;
        String userId = redisUtils.get(tokenKey);

        if (StringUtils.isEmpty(userId)) {
            return CourseResponseWrapper.getFailed("token无效");
        }

        // 验证JWT token
        if (!jwtTokenUtil.validateToken(token)) {
            // token无效，清理Redis
            redisUtils.delete(tokenKey);
            redisUtils.delete(AuthServerConstant.LOGIN_USER_ID_PREFIX + userId);
            return CourseResponseWrapper.getFailed("token无效");
        }

        // 验证通过，刷新token过期时间
        redisUtils.expire(tokenKey, AuthServerConstant.LOGIN_TOKEN_EXPIRE, TimeUnit.SECONDS);
        redisUtils.expire(AuthServerConstant.LOGIN_USER_ID_PREFIX + userId,
                AuthServerConstant.LOGIN_TOKEN_EXPIRE, TimeUnit.SECONDS);

        return CourseResponseWrapper.getSuccess();
    }

    /**
     * 退出登录
     */
    public CourseResponseWrapper logout(String token) {
        if (StringUtils.isEmpty(token)) {
            return CourseResponseWrapper.getFailed("退出失败");
        }

        String tokenKey = AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + token;
        String userId = redisUtils.get(tokenKey);

        if (StringUtils.isNotEmpty(userId)) {
            // 删除token和用户映射
            redisUtils.delete(tokenKey);
            redisUtils.delete(AuthServerConstant.LOGIN_USER_ID_PREFIX + userId);
            return CourseResponseWrapper.getSuccess();
        }

        return CourseResponseWrapper.getFailed("退出失败");
    }

    /**
     * 根据token获取用户ID
     */
    public CourseResponseWrapper getUserIdByToken(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String tokenKey = AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + token;
            String userIdStr = redisUtils.get(tokenKey);
            if (StringUtils.isNotEmpty(userIdStr)) {
                return CourseResponseWrapper.getSuccess(userIdStr);
            }
        }
        return CourseResponseWrapper.getFailed("token无效");
    }
}