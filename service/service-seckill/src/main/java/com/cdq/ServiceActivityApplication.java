package com.cdq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @program: ware-manage
 * @description:
 * @author: cdq
 * @create: 2021-06-14 02:59
 **/
@SpringBootApplication
@EnableDiscoveryClient
//@EnableFeignClients(basePackages= {"com.cdq"})
public class ServiceActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceActivityApplication.class, args);
    }
}
