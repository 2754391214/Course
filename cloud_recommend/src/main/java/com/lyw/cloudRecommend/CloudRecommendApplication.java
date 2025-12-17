package com.lyw.cloudRecommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages ={"com.lyw"})
public class CloudRecommendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudRecommendApplication.class, args);
    }

}
