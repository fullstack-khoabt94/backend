package com.eazybytes.task.services;

import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;
import com.eazybytes.task.repositories.TaskRepository;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Override
    public Task createTask(CreateTaskDto createTaskDto) {
        Task newTask = new Task();
        User user = userRepository.findById(createTaskDto.userId()).orElseThrow(() -> new NotFoundException("User"));
        newTask.setUser(user);
        newTask.setTitle(createTaskDto.title());
        newTask.setDescription(createTaskDto.description());
        newTask.setDueDate(createTaskDto.dueDate());
        newTask.setStatus(createTaskDto.status());
        newTask.setPriority(createTaskDto.priority());

        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(UUID taskId, UpdateTaskDto updateTaskDto) {
        Task updatedTask = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        updatedTask.setTitle(updateTaskDto.title());
        updatedTask.setDescription(updateTaskDto.description());
        updatedTask.setDueDate(updateTaskDto.dueDate());
        updatedTask.setStatus(updateTaskDto.status());
        updatedTask.setPriority(updateTaskDto.priority());

        return taskRepository.save(updatedTask);
    }

    @Override
    public List<Task> getTasks() {
        return List.of();
    }

    @Override
    public Task getTask(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
    }

    @Override
    public void deleteTask(UUID taskId) {
        Task willBeDeletedTask = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        taskRepository.delete(willBeDeletedTask);
    }
}