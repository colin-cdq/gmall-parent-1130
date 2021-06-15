package com.cdq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-06-03 23:32
 **/

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.cdq"})
public class ServerGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(ServerGatewayApplication.class,args);
    }
}
