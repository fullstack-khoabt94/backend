package com.eazybytes.task.services;

import com.eazybytes.exceptions.BadRequestException;
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
    public Task isTaskOwner(UUID ownerId, UUID taskId) throws BadRequestException {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        if (task.getUser().getId().equals(ownerId)) return task;
        else throw new BadRequestException("This owner does not own the task!");
    }

    @Override
    public Task createTask(UUID ownerId, CreateTaskDto createTaskDto) {
        Task newTask = new Task();
        User user = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User"));
        newTask.setUser(user);
        newTask.setTitle(createTaskDto.title());
        newTask.setDescription(createTaskDto.description());
        newTask.setDueDate(createTaskDto.dueDate());
        newTask.setStatus(createTaskDto.status());
        newTask.setPriority(createTaskDto.priority());

        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(UUID ownerId, UUID taskId, UpdateTaskDto updateTaskDto) {
        Task updatedTask = this.isTaskOwner(ownerId, taskId);
        updatedTask.setTitle(updateTaskDto.title());
        updatedTask.setDescription(updateTaskDto.description());
        updatedTask.setDueDate(updateTaskDto.dueDate());
        updatedTask.setStatus(updateTaskDto.status());
        updatedTask.setPriority(updateTaskDto.priority());

        return taskRepository.save(updatedTask);

    }

    @Override
    public List<Task> getTasks(UUID ownerId) {
        User user = userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User"));
        return this.taskRepository.findAllByUser(user);
    }

    @Override
    public Task getTask(UUID ownerId, UUID taskId) {
        return this.isTaskOwner(ownerId, taskId);
    }

    @Override
    public boolean deleteTask(UUID ownerId, UUID taskId) {
        Task willBeDeletedTask = this.isTaskOwner(ownerId, taskId);
        taskRepository.delete(willBeDeletedTask);
        return true;
    }
}