package com.eazybytes.task.dtos;

import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;
import com.eazybytes.tag.dtos.TagResponse;
import com.eazybytes.task.entity.Task;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDateTime dueDate,
        UUID boardId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Set<TagResponse> tags
) {
    public static TaskResponse fromTask(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getBoard().getId(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getTags().stream().map(TagResponse::fromTag).collect(Collectors.toSet())
        );
    }
}