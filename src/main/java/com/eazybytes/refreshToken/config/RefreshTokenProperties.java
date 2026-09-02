package com.eazybytes.refreshToken.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.refresh-token")
@Validated
public record RefreshTokenProperties(
        @NotBlank
        @Size(min = 32)
        String secret,

        @NotNull @Positive
        Long expirationMs
) {
}