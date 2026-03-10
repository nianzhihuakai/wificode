package com.nzhk.wificode.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Map;

/**
 * JWT 工具：生成/解析 token，claims 中存 userId
 */
public class JwtUtil {

    private static String secret = "wificode-jwt-secret-change-in-prod";

    public static void setSecret(String s) {
        secret = s;
    }

    public static String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .sign(Algorithm.HMAC256(secret));
    }

    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }
}
