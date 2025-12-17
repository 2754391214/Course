package com.lyw.cloudCourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages ={"com.lyw"})
public class CloudCourseApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudCourseApplication.class, args);
    }

}
