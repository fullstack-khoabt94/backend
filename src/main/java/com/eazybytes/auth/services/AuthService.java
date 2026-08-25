package com.eazybytes.auth.services;

import com.eazybytes.auth.dtos.LoginDto;
import com.eazybytes.auth.dtos.LoginResponse;
import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;

public interface AuthService {
    UserResponse signup(CreateUserDto createUserDto);

    LoginResponse login(LoginDto loginDto);
}