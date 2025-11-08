package com.lyw.cloudAuthServer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OAuth2配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {

    private Map<String, ClientConfig> clients;

    @Data
    public static class ClientConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUrl;
        private String userInfoUrl;
        private String grantType = "authorization_code";
    }
}