package com.eazybytes.task.services;

import com.eazybytes.board.entity.Board;
import com.eazybytes.board.repository.BoardRepository;
import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;
import com.eazybytes.task.repositories.TaskRepository;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import com.eazybytes.utils.Sorts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private static final Set<String> ALLOWED_SORT = Set.of(
            "createdAt",
            "dueDate",
            "priority"
    );
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;

    private Board getValidBoard(UUID boardId) {
        return this.boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board"));
    }

    @Override
    public TaskResponse createTask(CreateTaskDto createTaskDto) {
        Task newTask = new Task();
        Board board = this.getValidBoard(createTaskDto.boardId());

        newTask.setBoard(board);
        newTask.setTitle(createTaskDto.title());
        newTask.setDescription(createTaskDto.description());
        newTask.setDueDate(createTaskDto.dueDate());
        newTask.setStatus(createTaskDto.status());
        newTask.setPriority(createTaskDto.priority());

        Task savedTask = taskRepository.save(newTask);
        return TaskResponse.fromTask(savedTask);
    }

    @Override
    public TaskResponse updateTask(UUID taskId, UpdateTaskDto updateTaskDto) {
        Task updatedTask = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        updatedTask.setTitle(updateTaskDto.title());
        updatedTask.setDescription(updateTaskDto.description());
        updatedTask.setDueDate(updateTaskDto.dueDate());
        updatedTask.setStatus(updateTaskDto.status());
        updatedTask.setPriority(updateTaskDto.priority());

        Task savedTask = taskRepository.save(updatedTask);
        return TaskResponse.fromTask(savedTask);

    }

    @Override
    public PagedResponse<TaskResponse> getTasks(UUID boardId, Pageable pageable) {
        Pageable sanitizedPageable = Sorts.sanitize(pageable, ALLOWED_SORT, "id");
        Board board = this.getValidBoard(boardId);
        return PagedResponse.of(this.taskRepository.findByBoard(board, sanitizedPageable)
                .map(TaskResponse::fromTask));
    }

    @Override
    public TaskResponse getTask(UUID taskId) {
        Task found = this.taskRepository.findById(taskId).orElseThrow(() -> new NotFoundException("Task"));
        return TaskResponse.fromTask(found);
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