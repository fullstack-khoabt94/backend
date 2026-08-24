package com.eazybytes.auth.controllers;

import com.eazybytes.auth.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/signup")
    public ResponseEntity<String> signup() {
        this.authService.signup();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}