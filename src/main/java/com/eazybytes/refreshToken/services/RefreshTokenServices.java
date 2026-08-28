package com.eazybytes.refreshToken.services;

import com.eazybytes.refreshToken.dtos.RefreshTokenResponse;
import com.eazybytes.refreshToken.entity.RefreshToken;

import java.util.UUID;

public interface RefreshTokenServices {
    RefreshTokenResponse createRefreshToken(UUID userId);
    
    RefreshToken getValidRefreshToken(String refreshToken);

    void revokeRefreshToken(String refreshToken);
}