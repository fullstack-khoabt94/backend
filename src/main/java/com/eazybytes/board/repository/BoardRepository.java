package com.eazybytes.board.repository;

import com.eazybytes.board.entity.Board;
import com.eazybytes.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByUser(User user);
}