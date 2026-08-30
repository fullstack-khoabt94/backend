package com.eazybytes.task.controllers;

import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;
import com.eazybytes.task.services.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        Task newTask = this.taskService.createTask(createTaskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.fromTask(newTask));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskDto updateTaskDto
    ) {
        Task updatedTask = this.taskService.updateTask(taskId, updateTaskDto);
        return ResponseEntity.status(HttpStatus.OK).body(TaskResponse.fromTask(updatedTask));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable UUID taskId
    ) {
        Task task = this.taskService.getTask(taskId);
        return ResponseEntity.status(HttpStatus.OK).body(TaskResponse.fromTask(task));
    }

    @GetMapping("/all")
    public ResponseEntity<List<TaskResponse>> getAllTask(
            @RequestParam("boardId") UUID boardId
    ) {
        List<TaskResponse> taskList = this.taskService.getTasks(boardId).stream().map(TaskResponse::fromTask).toList();
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