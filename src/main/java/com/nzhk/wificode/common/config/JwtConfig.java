package com.nzhk.wificode.common.config;

import com.nzhk.wificode.common.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${wificode.jwt.secret:wificode-jwt-secret-change-in-prod}")
    private String secret;

    @PostConstruct
    public void init() {
        JwtUtil.setSecret(secret);
    }
}
