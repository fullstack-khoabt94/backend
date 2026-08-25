package com.eazybytes.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDto(
        @NotBlank
        @Size(max = 30, message = "Name length must not be longer than 30")
        String name,

        @NotBlank
        @Size(max = 50, message = "email length must not be longer than 30")
        String email,

        @NotBlank
        String password
) {
}