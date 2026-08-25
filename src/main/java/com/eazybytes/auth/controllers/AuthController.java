package com.eazybytes.auth.controllers;

import com.eazybytes.auth.services.AuthService;
import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        this.authService.login();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody CreateUserDto createUserDto) {
        UserResponse userResponse = this.authService.signup(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
}