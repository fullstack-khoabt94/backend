package com.eazybytes.auth.jwt;

import com.eazybytes.user.entity.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
        Map<String, Object> claims = new HashMap<>();

        claims.put("email", userInfo.getUsername());
        claims.put("roles", List.of("ROLE_USER"));


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

    public Collection<GrantedAuthority> getAuthoritiesFromJwt(String token) {
        List<String> roles = (List<String>) extractAllClaims(token).get("roles");
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(this.getSigningKey()).build().parse(token);
            return true;
        } catch (JwtException e) {
            return false;
        }

    }

}