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
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskDto createTaskDto
    ) {
        TaskResponse newTask = this.taskService.createTask(createTaskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskDto updateTaskDto
    ) {
        TaskResponse updatedTask = this.taskService.updateTask(taskId, updateTaskDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTask);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID taskId
    ) {
        TaskResponse task = this.taskService.getTask(taskId);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<TaskResponse>> getAllTask(
            @RequestParam UUID boardId,
            @PageableDefault(size = 20, sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PagedResponse<TaskResponse> taskList =
                this.taskService.getTasks(boardId, pageable);
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