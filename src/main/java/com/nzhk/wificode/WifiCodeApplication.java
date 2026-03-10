package com.nzhk.wificode;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.nzhk.wificode.**"})
@MapperScan(value = "com.nzhk.wificode.mapper")
@SpringBootApplication
public class WifiCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifiCodeApplication.class, args);
    }
}
