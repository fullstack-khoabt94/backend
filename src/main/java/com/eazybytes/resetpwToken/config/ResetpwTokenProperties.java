package com.eazybytes.resetpwToken.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.resetpw-token")
public record ResetpwTokenProperties(
        @NotBlank
        Long expirationMs
) {
}