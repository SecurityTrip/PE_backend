package com.example.sea_battle.jwt;

import com.example.sea_battle.services.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

import static io.jsonwebtoken.security.Keys.secretKeyFor;

@Component
public class JwtCore {
    @Value("${sea_battle.app.secret}")
    private String secret;
    @Value("${sea_battle.app.lifetime}")
    private long lifetime;

    private final SecretKey key = secretKeyFor(SignatureAlgorithm.HS512);

    public String generateToken(Authentication auth) {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date())
                        .getTime() + lifetime))
                .signWith(key)
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
                .signWith(key)
                .compact();
    }
}