package com.lyw.cloudChoose;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableDiscoveryClient
@EnableTransactionManagement
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages ={"com.lyw"})
public class CloudChooseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudChooseApplication.class, args);
    }

}
