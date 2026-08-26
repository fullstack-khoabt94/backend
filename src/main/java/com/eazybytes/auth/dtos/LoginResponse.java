package com.eazybytes.auth.dtos;

import com.eazybytes.user.dtos.UserResponse;

public record LoginResponse(
        UserResponse user,
        String accessToken
) {
}