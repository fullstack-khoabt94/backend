package com.eazybytes.task.dtos;

import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public record QueryTasksDto(
        List<TaskStatus> statuses,
        TaskPriority priority,
        String search,
        LocalDate dueOnOrBefore
) {
}