package com.eazybytes.refreshToken.dtos;

import com.eazybytes.refreshToken.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

public record RefreshTokenResponse(
        String refreshToken,
        UUID userId,
        LocalDateTime expiredAt
) {

    public static RefreshTokenResponse fromRefreshToken(RefreshToken refreshToken) {
        return new RefreshTokenResponse(
                refreshToken.getRefreshToken(),
                refreshToken.getUser().getId(),
                refreshToken.getExpiredAt()
        );
    }
}