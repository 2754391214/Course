package com.lyw.cloudRanking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableFeignClients
@SpringBootApplication
@ComponentScan(basePackages ={"com.lyw"})
public class CloudRankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudRankingApplication.class, args);
    }

}
