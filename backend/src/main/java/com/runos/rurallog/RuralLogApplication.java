package com.runos.rurallog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.runos.rurallog.mapper")
public class RuralLogApplication {
    public static void main(String[] args) {
        SpringApplication.run(RuralLogApplication.class, args);
    }
}
