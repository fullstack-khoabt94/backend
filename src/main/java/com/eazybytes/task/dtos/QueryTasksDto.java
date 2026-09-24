package com.eazybytes.task.dtos;

import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record QueryTasksDto(
        List<TaskStatus> statuses,
        TaskPriority priority,
        String search,
        LocalDate dueOnOrBefore,
        Set<UUID> tags
) {
}