package com.eazybytes.task.repositories;

import com.eazybytes.board.entity.Board;
import com.eazybytes.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    Page<Task> findByBoard(Board board, Pageable pageable);
}