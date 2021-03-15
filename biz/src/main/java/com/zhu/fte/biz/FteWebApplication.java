package com.zhu.fte.biz;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动器
 *
 * @author zhujiqian
 * @date 2020/7/29 22:53
 */
@SpringBootApplication
@EnableScheduling
public class FteWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(FteWebApplication.class,args);
    }
}
