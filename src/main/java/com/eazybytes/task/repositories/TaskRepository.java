package com.eazybytes.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eazybytes.task.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
}