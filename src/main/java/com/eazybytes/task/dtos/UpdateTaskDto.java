package com.eazybytes.task.dtos;

import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateTaskDto(
        @NotBlank(message = "Title must not be empty")
        @Size(min = 1, max = 120, message = "Title must be between 1 and 120 characters")
        String title,

        @NotBlank(message = "Description must not be empty")
        String description,

        @NotNull
        TaskStatus status,

        @Nullable
        @Future
        LocalDateTime dueDate,

        @NotNull
        TaskPriority priority
) {
}