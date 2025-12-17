package com.lyw.cloudThirdPart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class CloudThirdPartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudThirdPartApplication.class, args);
    }

}
