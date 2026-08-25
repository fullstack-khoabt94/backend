package com.eazybytes.auth.services;

import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;

public interface AuthService {
    UserResponse signup(CreateUserDto createUserDto);

    void login();
}