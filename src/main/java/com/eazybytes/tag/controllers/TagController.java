package com.eazybytes.tag.controllers;

import com.eazybytes.tag.dtos.CreateTagDto;
import com.eazybytes.tag.dtos.TagResponse;
import com.eazybytes.tag.dtos.UpdateTagDto;
import com.eazybytes.tag.services.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public ResponseEntity<TagResponse> createTag(
            @Valid @RequestBody CreateTagDto createTagDto,
            @AuthenticationPrincipal UUID userId
    ) {
        TagResponse newTag = this.tagService.createTag(userId, createTagDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newTag);
    }

    @PutMapping("/{tagId}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable UUID tagId,
            @Valid @RequestBody UpdateTagDto updateTagDto,
            @AuthenticationPrincipal UUID userId
    ) {
        TagResponse updatedTag = this.tagService.updateTag(userId, tagId, updateTagDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedTag);
    }

    @GetMapping("/{tagId}")
    public ResponseEntity<TagResponse> getTag(
            @PathVariable UUID tagId,
            @AuthenticationPrincipal UUID userId
    ) {
        TagResponse tag = this.tagService.getTag(userId, tagId);
        return ResponseEntity.status(HttpStatus.OK).body(tag);
    }

    @GetMapping("/all")
    public ResponseEntity<List<TagResponse>> getAllTags(
            @AuthenticationPrincipal UUID userId
    ) {
        List<TagResponse> tagList =
                this.tagService.getTags(userId);
        return ResponseEntity.status(HttpStatus.OK).body(tagList);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<String> deleteTag(
            @PathVariable UUID tagId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean isSuccess = this.tagService.deleteTag(userId, tagId);
        return ResponseEntity.status(HttpStatus.OK).body(isSuccess ? "Done" : "Can not delete");
    }
}