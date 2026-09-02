package com.eazybytes.resetpwToken.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.resetpw-token")
@Validated
public record ResetpwTokenProperties(
        @NotNull @Positive
        Long expirationMs
) {
}