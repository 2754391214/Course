package com.lyw.cloudAuthServer.factory;

import com.alibaba.fastjson.JSON;
import com.lyw.cloudAuthServer.config.OAuth2Properties;
import com.lyw.cloudAuthServer.dto.SocialUserDto;
import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import com.lyw.commonUtil.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2服务工厂
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2ServiceFactory {

    private final OAuth2Properties oauth2Properties;

    /**
     * 获取access_token
     */
    public CourseResponseWrapper getAccessToken(String socialType, String code) {
        try {
            OAuth2Properties.ClientConfig client = oauth2Properties.getClients().get(socialType);
            if (client == null) {
                log.error("不支持的社交登录类型: {}", socialType);
                return CourseResponseWrapper.getFailed("不支持的社交登录类型");
            }

            Map<String, String> params = new HashMap<>();
            params.put("client_id", client.getClientId());
            params.put("client_secret", client.getClientSecret());
            params.put("grant_type", client.getGrantType());
            params.put("redirect_uri", client.getRedirectUri());
            params.put("code", code);

            String[] urlParts = parseUrl(client.getTokenUrl());
            String host = urlParts[0];
            String path = urlParts[1];

            log.info("获取{} access_token, 请求地址: {}{}", socialType, host, path);
            HttpResponse response = HttpUtils.doPost(host, path, "post",
                    new HashMap<>(), params, new HashMap<>());

            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity());
                SocialUserDto socialUser = JSON.parseObject(json, SocialUserDto.class);

                if (socialUser != null && StringUtils.isNotEmpty(socialUser.getAccess_token())) {
                    socialUser.setSocialType(socialType);
                    log.info("成功获取{} access_token", socialType);
                    return CourseResponseWrapper.getSuccess(socialUser);
                } else {
                    log.error("{} access_token解析失败", socialType);
                    return CourseResponseWrapper.getFailed(socialType + "授权信息解析失败");
                }
            } else {
                String errorMsg = EntityUtils.toString(response.getEntity());
                log.error("获取{} access_token失败，状态码: {}, 错误信息: {}",
                        socialType, response.getStatusLine().getStatusCode(), errorMsg);
                return CourseResponseWrapper.getFailed(socialType + "授权失败");
            }

        } catch (Exception e) {
            log.error("获取{} access_token异常: {}", socialType, e.getMessage(), e);
            return CourseResponseWrapper.getFailed(socialType + "授权异常");
        }
    }

    /**
     * 获取用户信息
     */
    public CourseResponseWrapper getUserInfo(String socialType, SocialUserDto socialUser) {
        try {
            OAuth2Properties.ClientConfig client = oauth2Properties.getClients().get(socialType);
            if (client == null || StringUtils.isEmpty(client.getUserInfoUrl())) {
                log.warn("{} 未配置用户信息接口或不需要获取用户信息", socialType);
                return CourseResponseWrapper.getSuccess(socialUser);
            }

            Map<String, String> params = buildUserInfoParams(socialType, socialUser);
            String[] urlParts = parseUrl(client.getUserInfoUrl());
            String host = urlParts[0];
            String path = urlParts[1];

            log.info("获取{}用户信息, 请求地址: {}{}", socialType, host, path);
            HttpResponse response = HttpUtils.doGet(host, path, "get", new HashMap<>(), params);

            if (response.getStatusLine().getStatusCode() == 200) {
                String userJson = EntityUtils.toString(response.getEntity());
                SocialUserDto userInfo = JSON.parseObject(userJson, SocialUserDto.class);

                // 复制用户信息，保留token相关字段
                String[] ignoreProperties = getIgnoreProperties(socialType);
                BeanUtils.copyProperties(userInfo, socialUser, ignoreProperties);

                log.info("成功获取{}用户信息: {}", socialType, userInfo.getName());
                return CourseResponseWrapper.getSuccess(socialUser);
            } else {
                String errorMsg = EntityUtils.toString(response.getEntity());
                log.error("获取{}用户信息失败，状态码: {}, 错误信息: {}",
                        socialType, response.getStatusLine().getStatusCode(), errorMsg);
                return CourseResponseWrapper.getFailed("获取" + socialType + "用户信息失败");
            }

        } catch (Exception e) {
            log.error("获取{}用户信息异常: {}", socialType, e.getMessage(), e);
            return CourseResponseWrapper.getFailed("获取" + socialType + "用户信息异常");
        }
    }

    /**
     * 构建用户信息请求参数
     */
    private Map<String, String> buildUserInfoParams(String socialType, SocialUserDto socialUser) {
        Map<String, String> params = new HashMap<>();

        switch (socialType.toLowerCase()) {
            case "gitee":
                params.put("access_token", socialUser.getAccess_token());
                break;
            case "weibo":
                params.put("access_token", socialUser.getAccess_token());
                params.put("uid", socialUser.getUid());
                break;
            case "github":
                params.put("access_token", socialUser.getAccess_token());
                break;
            default:
                params.put("access_token", socialUser.getAccess_token());
        }

        return params;
    }

    /**
     * 获取需要忽略复制的属性
     */
    private String[] getIgnoreProperties(String socialType) {
        switch (socialType.toLowerCase()) {
            case "gitee":
                return new String[]{"access_token", "token_type", "refresh_token", "scope", "create_at"};
            case "weibo":
                return new String[]{"access_token", "remind_in", "expires_in"};
            default:
                return new String[]{"access_token", "token_type", "refresh_token", "scope"};
        }
    }

    /**
     * 解析URL
     */
    private String[] parseUrl(String url) {
        if (url.startsWith("https://")) {
            url = url.substring(8);
        } else if (url.startsWith("http://")) {
            url = url.substring(7);
        }

        int slashIndex = url.indexOf('/');
        if (slashIndex == -1) {
            return new String[]{url, "/"};
        }

        String host = url.substring(0, slashIndex);
        String path = url.substring(slashIndex);
        return new String[]{host, path};
    }

    /**
     * 验证社交类型是否支持
     */
    public boolean isSocialTypeSupported(String socialType) {
        return oauth2Properties.getClients().containsKey(socialType);
    }

    /**
     * 获取支持的社交登录类型
     */
    public String getSupportedSocialTypes() {
        return String.join(", ", oauth2Properties.getClients().keySet());
    }
}