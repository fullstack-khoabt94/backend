package com.eazybytes.tag.dtos;

import com.eazybytes.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.UUID;

public record TagResponse(
        UUID id,
        String title,
        String description,
        String color,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TagResponse fromTag(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getTitle(),
                tag.getDescription(),
                tag.getColor(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}