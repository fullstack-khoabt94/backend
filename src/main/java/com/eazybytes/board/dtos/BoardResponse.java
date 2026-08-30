package com.eazybytes.board.dtos;

import com.eazybytes.board.entity.Board;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardResponse(
        UUID id,
        String title,
        String description,
        String color,
        String icon,
        Boolean isArchived,
        UUID userId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BoardResponse fromBoard(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getDescription(),
                board.getColor(),
                board.getIcon(),
                board.getIsArchived(),
                board.getUser().getId(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}