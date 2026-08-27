package com.eazybytes.task.services;

import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    Task isTaskOwner(UUID ownerId, UUID taskId) throws BadRequestException;

    Task createTask(UUID ownerId, CreateTaskDto createTaskDto);

    Task updateTask(UUID ownerId, UUID taskId, UpdateTaskDto updateTaskDto);

    List<Task> getTasks(UUID ownerId);

    Task getTask(UUID ownerId, UUID taskID);

    boolean deleteTask(UUID ownerId, UUID taskID);
}