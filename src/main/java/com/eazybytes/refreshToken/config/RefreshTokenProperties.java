package com.eazybytes.refreshToken.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.refresh-token")
public record RefreshTokenProperties(
        @NotBlank
        String secret,

        @NotBlank
        Long expirationMs
) {
}