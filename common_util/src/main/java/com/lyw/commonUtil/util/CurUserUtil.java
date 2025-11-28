package com.lyw.commonUtil.util;

import cn.hutool.core.util.StrUtil;
import com.lyw.commonUtil.constant.AuthServerConstant;
import com.lyw.commonUtil.vo.SysUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class CurUserUtil {

    public static final String finalUser = "1";

    // 注意：这里不能直接注入，因为这是静态工具类
    private static RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置RedisTemplate（需要在配置类中初始化）
     */
    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        CurUserUtil.redisTemplate = redisTemplate;
    }

    public static HttpServletRequest getRequest(){
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null)
            return null;
        return ((ServletRequestAttributes)requestAttributes).getRequest();
    }

    /**
     * 获取当前登录用户的User ID
     * @return
     */
    public static String getUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            String userId = request.getHeader("X-User-Id");
            if (StrUtil.isNotEmpty(userId)) {
                return userId;
            }
        }
        return finalUser;
    }

    /**
     * 获取当前登录用户的User ID (Long类型)
     * @return
     */
    public static Long getUserIdLong() {
        String userId = getUserId();
        if (finalUser.equals(userId)) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            log.error("用户ID格式错误: {}", userId);
            return null;
        }
    }

    /**
     * 获取当前登录用户的User Code
     * @return
     */
    public static String getUserCode() {
        // 这里可以根据实际情况调整，如果用户代码和ID不同，可能需要从其他途径获取
        return getUserId();
    }

    /**
     * 获取当前登录用户的Token
     * @return
     */
    public static String getToken() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            String token = request.getHeader("X-User-Token");
            if (StrUtil.isNotEmpty(token)) {
                return token;
            }

            // 如果X-User-Token没有，尝试从Authorization头获取
            String authHeader = request.getHeader("Authorization");
            if (StrUtil.isNotEmpty(authHeader) && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户完整信息
     * 注意：这个方法需要Redis支持，且用户信息需要预先存储在Redis中
     */
    public static SysUserVo getCurrentLoginUser(){
        try {
            String token = getToken();
            if (StrUtil.isBlank(token) || redisTemplate == null) {
                return null;
            }

            String tokenKey = AuthServerConstant.LOGIN_USER_TOKEN_PREFIX + token;
            // 从Redis获取用户ID
            String userId = (String) redisTemplate.opsForValue().get(tokenKey);
            if (StrUtil.isBlank(userId)) {
                return null;
            }

            // 从Redis获取用户完整信息
            String userKey = AuthServerConstant.LOGIN_USER_ID_PREFIX + userId;
            SysUserVo userVo = (SysUserVo) redisTemplate.opsForValue().get(userKey);
            return userVo;
        } catch (Exception e) {
            log.error("获取当前登录用户信息失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断当前用户是否已登录
     */
    public static boolean isLogin() {
        String userId = getUserId();
        return !finalUser.equals(userId) && StrUtil.isNotEmpty(userId);
    }

    /**
     * 判断当前用户是否是机器用户（未登录）
     */
    public static boolean isMachineUser() {
        return finalUser.equals(getUserId());
    }
}