package com.eazybytes.auth.controllers;

import com.eazybytes.auth.services.IAuthServices;
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
public class AuthControllers {

    private final IAuthServices authServices;

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        this.authServices.login();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @GetMapping("/signup")
    public ResponseEntity<String> signup() {
        this.authServices.signup();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}