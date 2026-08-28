package com.eazybytes.auth.controllers;

import com.eazybytes.auth.dtos.LoginDto;
import com.eazybytes.auth.dtos.LoginResponse;
import com.eazybytes.auth.services.AuthService;
import com.eazybytes.refreshToken.dtos.RefreshTokenDto;
import com.eazybytes.resetpwToken.dtos.ResetPasswordRequestDto;
import com.eazybytes.resetpwToken.dtos.ResetpwTokenRequestDto;
import com.eazybytes.resetpwToken.services.ResetpwTokenService;
import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ResetpwTokenService resetpwTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginDto) {
        LoginResponse loginResponse = this.authService.login(loginDto);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody CreateUserDto createUserDto) {
        UserResponse userResponse = this.authService.signup(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenDto refreshTokenDto) {
        LoginResponse loginResponse = this.authService.refreshToken(refreshTokenDto);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }

    @PostMapping("/request-reset-password-token")
    public ResponseEntity<String> requestResetPasswordToken(@Valid @RequestBody ResetpwTokenRequestDto resetpwTokenRequestDto) {
        this.resetpwTokenService.createResetpwToken(resetpwTokenRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Token issued!");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequestDto resetPasswordRequestDto) {
        this.authService.resetPassword(resetPasswordRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body("Success!");
    }
}