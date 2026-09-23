package com.eazybytes.tag.services;

import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.tag.dtos.CreateTagDto;
import com.eazybytes.tag.dtos.TagResponse;
import com.eazybytes.tag.dtos.UpdateTagDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TagService {

    TagResponse createTag(UUID userId, CreateTagDto createTagDto);

    TagResponse updateTag(UUID userId, UUID tagId, UpdateTagDto updateTagDto);

    List<TagResponse> getTags(UUID userId);

    TagResponse getTag(UUID userId, UUID tagID);

    boolean deleteTag(UUID ownerId, UUID tagID);
}