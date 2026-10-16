package com.dsf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dsf.mapper")
public class Staff6Application {
    public static void main(String[] args) {
        SpringApplication.run(Staff6Application.class,args);
    }
}
