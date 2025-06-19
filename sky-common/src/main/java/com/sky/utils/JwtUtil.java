package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    /**
     * 生成jwt
     * 使用 HS256 算法，秘钥必须 >= 256 bits（32字节）
     *
     * @param secretKey jwt秘钥
     * @param ttlMillis jwt过期时间(毫秒)
     * @param claims    载荷数据
     * @return 生成的 JWT 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis);

        // 安全方式构建密钥
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .signWith(key, signatureAlgorithm)
                .setExpiration(exp);

        return builder.compact();
    }

    /**
     * 解析 JWT
     *
     * @param secretKey jwt秘钥
     * @param token     令牌
     * @return 解密出的 claims 信息
     */
    public static Claims parseJWT(String secretKey, String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
