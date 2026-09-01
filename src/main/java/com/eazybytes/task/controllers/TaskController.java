package com.eazybytes.task.controllers;

import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/board/{boardId}/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskDto createTaskDto,
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID boardId
    ) {
        TaskResponse newTask = this.taskService.createTask(userId, boardId, createTaskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @PathVariable UUID boardId,
            @Valid @RequestBody UpdateTaskDto updateTaskDto,
            @AuthenticationPrincipal UUID userId
    ) {
        TaskResponse updatedTask = this.taskService.updateTask(userId, boardId, taskId, updateTaskDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID taskId,
            @PathVariable UUID boardId,
            @AuthenticationPrincipal UUID userId
    ) {
        TaskResponse task = this.taskService.getTask(userId, boardId, taskId);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<TaskResponse>> getAllTask(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID boardId,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<TaskResponse> taskList =
                this.taskService.getTasks(userId, boardId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(taskList);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean isSuccess = this.taskService.deleteTask(userId, taskId);
        return ResponseEntity.status(HttpStatus.OK).body(isSuccess ? "Done" : "Can not delete");
    }
}