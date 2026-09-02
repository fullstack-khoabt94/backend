package com.eazybytes.board.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateBoardDto(
        @NotBlank(message = "Title must not be empty")
        @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
        String title,

        @NotBlank(message = "Description must not be empty")
        String description,

        String color,

        String icon
) {
}