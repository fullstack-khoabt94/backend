package com.eazybytes.auth.jwt;

public record AccessTokenResult(
        String accessToken,
        Long accessTokenExpiresIn
) {
}