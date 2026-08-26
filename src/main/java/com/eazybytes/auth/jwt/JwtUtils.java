package com.eazybytes.auth.jwt;

import com.eazybytes.user.entity.UserInfo;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.jwtProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        Map<String, String> claims = new HashMap<>();

        claims.put("email", userInfo.getUsername());
        claims.put("roles", userInfo.getAuthorities().toString());


        return Jwts.builder()
                .subject(userInfo.getId().toString())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + jwtProperties.expirationMs()))
                .signWith(this.getSigningKey())
                .claims(claims)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(this.getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromJwt(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(this.getSigningKey()).build().parse(token);
            return true;
        } catch (MalformedJwtException | ExpiredJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return false;
        }

    }

}