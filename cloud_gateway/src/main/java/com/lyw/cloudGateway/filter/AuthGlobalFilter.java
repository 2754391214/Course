package com.lyw.cloudGateway.filter;

import com.lyw.cloudGateway.service.AuthWebClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthWebClientService authWebClientService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 不需要认证的路径
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/sms/sendCode",
            "/api/auth/validateToken",
            "/api/thirdpart/**",
            "/api/course/public/**"
    );

    public AuthGlobalFilter(AuthWebClientService authWebClientService) {
        this.authWebClientService = authWebClientService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.info("请求路径: {}", path);

        // 白名单直接放行
        for (String pattern : WHITE_LIST) {
            if (pathMatcher.match(pattern, path)) {
                return chain.filter(exchange);
            }
        }

        // 从请求头中提取token
        String token = extractToken(request);
        if (StringUtils.isEmpty(token)) {
            return unauthorizedResponse(exchange, "缺少访问令牌");
        }

        // 使用 WebClient 验证 token 并获取用户ID
        return authWebClientService.validateToken(token)
                .flatMap(valid -> {
                    if (valid) {
                        return authWebClientService.getUserIdByToken(token)
                                .flatMap(userId -> {
                                    // 将用户ID添加到header中传递给下游服务
                                    ServerHttpRequest newRequest = request.mutate()
                                            .header("X-User-Id", userId)
                                            .header("X-User-Token", token)
                                            .build();
                                    return chain.filter(exchange.mutate().request(newRequest).build());
                                })
                                .switchIfEmpty(unauthorizedResponse(exchange, "获取用户信息失败"));
                    } else {
                        return unauthorizedResponse(exchange, "令牌无效或已过期");
                    }
                });
    }

    /**
     * 从请求头中提取token
     */
    private String extractToken(ServerHttpRequest request) {
        List<String> headers = request.getHeaders().get("Authorization");
        if (headers != null && !headers.isEmpty()) {
            String bearerToken = headers.get(0);
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }
        return null;
    }

    /**
     * 返回未授权响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String body = String.format("{\"code\": 401, \"message\": \"%s\", \"success\": false}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}