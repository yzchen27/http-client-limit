package com.thread0.demo;

//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;

import com.thread0.demo.bean.HttpClientUtil;
import com.thread0.demo.bean.HttpClientUtilBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.thread0"})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public HttpClientUtil httpClientUtil() {
        return HttpClientUtilBuilder.builder().build();
    }
}
