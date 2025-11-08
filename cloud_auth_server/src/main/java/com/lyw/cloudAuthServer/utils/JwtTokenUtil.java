package com.lyw.cloudAuthServer.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.lyw.cloudAuthServer.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

    public JwtTokenUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * 生成token
     */
    public String generateToken(String userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + jwtProperties.getExpire() * 1000);

        return JWT.create()
                .withSubject(userId)
                .withClaim("username", username)
                .withClaim("userId", userId)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(jwtProperties.getSecret()));
    }

    /**
     * 验证token
     */
    public boolean validateToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            log.error("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            return jwt.getClaim("userId").asLong();
        } catch (Exception e) {
            log.error("从token获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtProperties.getSecret()))
                    .build()
                    .verify(token);
            return jwt.getClaim("username").asString();
        } catch (Exception e) {
            log.error("从token获取用户名失败: {}", e.getMessage());
            return null;
        }
    }
}