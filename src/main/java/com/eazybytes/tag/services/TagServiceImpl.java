package com.eazybytes.tag.services;

import com.eazybytes.board.entity.Board;
import com.eazybytes.board.services.BoardService;
import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.tag.dtos.CreateTagDto;
import com.eazybytes.tag.dtos.TagResponse;
import com.eazybytes.tag.dtos.UpdateTagDto;
import com.eazybytes.tag.entity.Tag;
import com.eazybytes.tag.repositories.TagRepository;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.QueryTasksDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;
import com.eazybytes.task.repositories.TaskRepository;
import com.eazybytes.task.services.TaskSpecification;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import com.eazybytes.utils.Sorts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final UserRepository userRepository;

    private Tag getValidTag(UUID userId, UUID tagId) {
        Tag tag = this.tagRepository.findById(tagId).orElseThrow(() -> new NotFoundException("Tag"));
        if (!tag.getUser().getId().equals(userId)) throw new NotFoundException("Tag");
        return tag;
    }

    @Override
    public TagResponse createTag(UUID userId, CreateTagDto createTagDto) {
        Tag newTag = new Tag();
        User user = this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User"));
        newTag.setUser(user);
        newTag.setTitle(createTagDto.title());
        newTag.setDescription(createTagDto.description());
        newTag.setColor(createTagDto.color());

        tagRepository.save(newTag);
        return TagResponse.fromTag(newTag);
    }

    @Override
    public TagResponse updateTag(UUID userId, UUID tagId, UpdateTagDto updateTagDto) {
        Tag updatedTag = this.getValidTag(userId, tagId);
        updatedTag.setTitle(updateTagDto.title());
        updatedTag.setDescription(updateTagDto.description());
        updatedTag.setColor(updateTagDto.color());

        Tag savedTag = tagRepository.save(updatedTag);
        return TagResponse.fromTag(savedTag);

    }

    @Override
    public List<TagResponse> getTags(UUID userId) {
        return this.tagRepository.findByUserId(userId).stream()
                .map(TagResponse::fromTag).toList();
    }

    @Override
    public TagResponse getTag(UUID userId, UUID tagId) {
        return TagResponse.fromTag(this.getValidTag(userId, tagId));
    }

    @Override
    public boolean deleteTag(UUID ownerId, UUID tagId) {
        Tag willBeDeletedTag = this.getValidTag(ownerId,tagId);
        tagRepository.delete(willBeDeletedTag);
        return true;
    }
}