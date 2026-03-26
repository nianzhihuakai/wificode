package com.nzhk.wificode.common.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Map;

/**
 * JWT 工具：生成/解析 token，claims 中存 userId
 */
public class JwtUtil {

    /**
     * 解析 token 中的 userId；无效或缺失时返回 null（用于可选登录的公开接口）
     */
    public static String tryParseUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> m = parseToken(token);
            Object id = m.get("userId");
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

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
