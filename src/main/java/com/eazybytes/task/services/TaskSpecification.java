package com.eazybytes.task.services;

import com.eazybytes.board.entity.Board;
import com.eazybytes.task.dtos.QueryTasksDto;
import com.eazybytes.task.entity.Task;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public final class TaskSpecification {

    private TaskSpecification() {};

    public static Specification<Task> from(Board board, QueryTasksDto queryTasksDto) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("board"), board));

            if (StringUtils.hasText(queryTasksDto.search())) {
                String like = "%" + queryTasksDto.search().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), like));
            }

            if (queryTasksDto.statuses() != null && !queryTasksDto.statuses().isEmpty()) {
                predicates.add(root.get("status").in(queryTasksDto.statuses()));
            }

            if (queryTasksDto.priority() != null) {
                predicates.add(cb.equal(root.get("priority"), queryTasksDto.priority()));
            }

            if (queryTasksDto.dueOnOrBefore() != null) {
                predicates.add(cb.lessThan(root.get("dueDate"), queryTasksDto.dueOnOrBefore().atStartOfDay().plusDays(1)));
            }

            return cb.and(predicates);
        };
    }
}