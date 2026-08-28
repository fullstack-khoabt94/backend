package com.eazybytes.auth.dtos;

import com.eazybytes.user.dtos.UserResponse;

import java.time.LocalDateTime;

public record LoginResponse(
        UserResponse user,
        String accessToken,
        String refreshToken,
        Long accessTokenExpiresIn,
        LocalDateTime refreshTokenExpiresIn
) {
}