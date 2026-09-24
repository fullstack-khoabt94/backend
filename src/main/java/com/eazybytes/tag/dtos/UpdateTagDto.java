package com.eazybytes.tag.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTagDto(
        @NotBlank(message = "Title must not be empty")
        @Size(min = 1, max = 120, message = "Title must be between 1 and 120 characters")
        String title,

        @NotBlank(message = "Description must not be empty")
        String description,

        @NotBlank(message = "Color must not be empty")
        String color
) {
}