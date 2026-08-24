package com.eazybytes.task.dtos;

import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;
import com.eazybytes.task.entity.Task;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskResponse(
       UUID id,

                 String title,

 String description,

TaskStatus status,

TaskPriority priority,

LocalDateTime dueDate,

UUID userId,
       LocalDateTime createdAt,
       LocalDateTime updatedAt
) {
    public static TaskResponse fromTask(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getUser().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}