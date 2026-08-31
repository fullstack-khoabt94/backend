package com.eazybytes.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class Sorts {
    public static Pageable sanitize(Pageable p, Set<String> allows, String tieBreaker) {
        String getValidTiebreaker = tieBreaker == null ? "id" : tieBreaker;
        Sort safe = Sort.by(
                p.getSort().stream().filter(s -> allows.contains(s.getProperty())).toList()
        );
        if (!safe.isUnsorted()) safe = safe.and(Sort.by(Sort.Direction.DESC, getValidTiebreaker));
        
        return PageRequest.of(p.getPageNumber(), p.getPageSize(), safe);
    }
}