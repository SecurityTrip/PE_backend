package com.example.sea_battle.jwt;

import com.example.sea_battle.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtCore {
    @Value("${sea_battle.app.secret}")
    private String secret;
    @Value("${sea_battle.app.lifetime}")
    private long lifetime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Создаем ключ достаточной длины для HS512
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] paddedKey = new byte[64]; // 512 бит для HS512
        System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, paddedKey.length));
        this.key = new SecretKeySpec(paddedKey, SignatureAlgorithm.HS512.getJcaName());
    }

    public String generateToken(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date())
                        .getTime() + lifetime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getNameFromJwt(String jwt) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String getUsernameFromJwt(String jwt) {
        return getNameFromJwt(jwt).getSubject();
    }

    public String refreshToken(String jwt) {
        Claims claims = getNameFromJwt(jwt);
        return Jwts.builder()
                .subject(claims.getSubject())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + lifetime))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}