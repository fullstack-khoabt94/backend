package com.eazybytes.user.services;

import com.eazybytes.user.entity.User;

import java.util.UUID;

public interface UserService {
    User getUser(UUID userId);
}