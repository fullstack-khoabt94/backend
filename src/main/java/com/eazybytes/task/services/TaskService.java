package com.eazybytes.task.services;

import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(UUID userId, UUID boardId, CreateTaskDto createTaskDto);

    TaskResponse updateTask(UUID userId, UUID boardId, UUID taskId, UpdateTaskDto updateTaskDto);

    PagedResponse<TaskResponse> getTasks(UUID userId, UUID boardId, Pageable pageable);

    TaskResponse getTask(UUID userId, UUID boardId, UUID taskID);

    boolean deleteTask(UUID ownerId, UUID taskID);
}