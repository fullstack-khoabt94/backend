package com.eazybytes.task.services;

import com.eazybytes.board.entity.Board;
import com.eazybytes.board.repository.BoardRepository;
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
    private final BoardRepository boardRepository;

    @Override
    public Task createTask(CreateTaskDto createTaskDto) {
        Task newTask = new Task();
        Board board = this.boardRepository.findById(createTaskDto.boardId()).orElseThrow(() -> new NotFoundException("Board"));

        newTask.setBoard(board);
        newTask.setTitle(createTaskDto.title());
        newTask.setDescription(createTaskDto.description());
        newTask.setDueDate(createTaskDto.dueDate());
        newTask.setStatus(createTaskDto.status());
        newTask.setPriority(createTaskDto.priority());

        return taskRepository.save(newTask);
    }

    @Override
    public Task updateTask(UUID taskId, UpdateTaskDto updateTaskDto) {
        Task updatedTask = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        updatedTask.setTitle(updateTaskDto.title());
        updatedTask.setDescription(updateTaskDto.description());
        updatedTask.setDueDate(updateTaskDto.dueDate());
        updatedTask.setStatus(updateTaskDto.status());
        updatedTask.setPriority(updateTaskDto.priority());

        return taskRepository.save(updatedTask);

    }

    @Override
    public List<Task> getTasks(UUID boardId) {
        return this.boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board")).getTasks();
    }

    @Override
    public Task getTask(UUID taskId) {
        return this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
    }

    @Override
    public boolean deleteTask(UUID ownerId, UUID taskId) {
        Task willBeDeletedTask = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        User user = this.userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User"));
        if (!willBeDeletedTask.getBoard().getUser().getId().equals(user.getId()))
            throw new BadRequestException("You are not the owner of the task, so you can't not delete it");
        taskRepository.delete(willBeDeletedTask);
        return true;
    }
}