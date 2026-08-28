package com.eazybytes.resetpwToken.dtos;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequestDto(
        @NotBlank
        String resetpwToken,

        @NotBlank
        String newPassword
) {
}