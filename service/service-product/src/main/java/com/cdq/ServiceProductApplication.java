package com.cdq;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @program: gmall-parent-1130
 * @description:
 * @author: cdq
 * @create: 2021-05-18 01:32
 **/
@SpringBootApplication//(exclude = DataSourceAutoConfiguration.class)//取消数据源自动配置
@MapperScan( "com.cdq.product.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages= {"com.cdq"})
public class ServiceProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceProductApplication.class, args);
    }

}
