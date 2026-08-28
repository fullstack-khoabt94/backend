package com.eazybytes.refreshToken.services;

import com.eazybytes.refreshToken.dtos.RefreshTokenResponse;
import com.eazybytes.refreshToken.entity.RefreshToken;

import java.util.UUID;

public interface RefreshTokenService {
    RefreshTokenResponse createRefreshToken(UUID userId);

    RefreshToken getValidRefreshToken(String refreshToken);

    void revokeAllByUserId(UUID userId);
}