package com.lyw.cloudGateway.service;

import com.lyw.commonUtil.responseWrapper.CourseResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class AuthWebClientService {

    private final WebClient webClient;

    @Value("${service.urls.auth-service:cloudAuthServer}")
    private String authServiceUrl;

    public AuthWebClientService(@LoadBalanced WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * 验证 Token
     */
    public Mono<Boolean> validateToken(String token) {
        return webClient.get()
                .uri("http://" + authServiceUrl + "/validateToken")
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(CourseResponseWrapper.class)
                .map(CourseResponseWrapper::isSuccess)
                .doOnSuccess(valid -> {
                    if (!valid) {
                        log.warn("Token验证失败: {}", token);
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("Token验证请求失败: {}", throwable.getMessage());
                    return Mono.just(false);
                });
    }

    /**
     * 根据 Token 获取用户ID
     */
    public Mono<String> getUserIdByToken(String token) {
        return webClient.get()
                .uri("http://" + authServiceUrl + "/getUserIdByToken")
                .header(HttpHeaders.AUTHORIZATION, token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(CourseResponseWrapper.class)
                .flatMap(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        return Mono.just(response.getData().toString());
                    } else {
                        log.warn("获取用户ID失败: {}", response.getErrorMessage());
                        return Mono.empty();
                    }
                })
                .onErrorResume(throwable -> {
                    log.error("获取用户ID请求失败: {}", throwable.getMessage());
                    return Mono.empty();
                });
    }
}