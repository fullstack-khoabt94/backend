package com.eazybytes.auth.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthControllers {

    @PostMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @GetMapping("/signup")
    public ResponseEntity<String> signup() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }
}