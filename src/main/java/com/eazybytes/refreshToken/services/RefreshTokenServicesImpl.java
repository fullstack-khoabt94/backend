package com.eazybytes.refreshToken.services;

import com.eazybytes.refreshToken.config.RefreshTokenProperties;
import com.eazybytes.refreshToken.dtos.RefreshTokenResponse;
import com.eazybytes.refreshToken.entity.RefreshToken;
import com.eazybytes.refreshToken.repository.RefreshTokenRepository;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServicesImpl implements RefreshTokenServices {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final RefreshTokenProperties refreshTokenProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.refreshTokenProperties.secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public RefreshTokenResponse createRefreshToken(UUID userId) {
        User user = this.userRepository.findById(userId).orElseThrow();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        LocalDateTime expiredAt = LocalDateTime.now().plus(Duration.ofMillis(this.refreshTokenProperties.expirationMs()));

        refreshToken.setRefreshToken(Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(this.getSigningKey())
                .compact());

        refreshToken.setExpiredAt(expiredAt);

        RefreshToken newRefreshToken = this.refreshTokenRepository.save(refreshToken);
        return RefreshTokenResponse.fromRefreshToken(newRefreshToken);
    }

    @Override
    public boolean checkValidRefreshToken(String refreshToken) {
        RefreshToken findRefreshToken = this.getRefreshTokenInfo(refreshToken);

        return !findRefreshToken.getIsRevoked() && LocalDateTime.now().isBefore(findRefreshToken.getExpiredAt());
    }

    @Override
    public RefreshToken getRefreshTokenInfo(String refreshToken) {
        return this.refreshTokenRepository.findRefreshTokenByRefreshToken(refreshToken).orElseThrow();
    }

    @Override
    public void revokeRefreshToken(String refreshToken) {
        RefreshToken findRefreshToken = this.getRefreshTokenInfo(refreshToken);
        findRefreshToken.setIsRevoked(true);
        findRefreshToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(findRefreshToken);
    }
}