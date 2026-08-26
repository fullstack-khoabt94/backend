package com.eazybytes.auth.jwt;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank
        String secret,

        @NotBlank
        Long expirationMs
) {
}