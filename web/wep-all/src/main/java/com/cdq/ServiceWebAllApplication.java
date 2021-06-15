package com.cdq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-21 01:33
 **/
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.cdq"})
public class ServiceWebAllApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceWebAllApplication.class, args);
    }
}
