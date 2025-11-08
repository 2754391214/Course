package com.lyw.cloudAuthServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.lyw.cloudAuthServer.feign")
@EnableDiscoveryClient
@SpringBootApplication
public class CloudAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudAuthServerApplication.class, args);
    }

}
