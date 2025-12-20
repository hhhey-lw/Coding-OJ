package com.longoj.top.infrastructure.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenUtil {

    private final static String secret = "K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=longcoding";

    private final static Long expiration = 604800L;

    public final static String tokenHeader = "Authorization";
    
    // 生成密钥
    private static SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    // 生成token
    public static String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // 从token中获取用户名
    public static Long getUserIdFromToken(String token) {
        return Long.valueOf(Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());
    }
    
    // 验证token
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSecretKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}