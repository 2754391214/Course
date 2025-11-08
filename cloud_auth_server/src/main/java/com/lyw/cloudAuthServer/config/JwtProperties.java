package com.lyw.cloudAuthServer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "your-jwt-secret-key-here-make-it-long-enough";
    private Long expire = 7 * 24 * 60 * 60L; // 7天
    private String header = "Authorization";
    private String prefix = "Bearer ";
}