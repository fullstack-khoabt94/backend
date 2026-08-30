package com.eazybytes.task.services;

import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    Task createTask(CreateTaskDto createTaskDto);

    Task updateTask(UUID taskId, UpdateTaskDto updateTaskDto);

    List<Task> getTasks(UUID boardId);

    Task getTask(UUID taskID);

    boolean deleteTask(UUID ownerId, UUID taskID);
}