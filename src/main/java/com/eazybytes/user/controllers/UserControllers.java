package com.eazybytes.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user")
public class UserControllers {

    @GetMapping("/me")
    public ResponseEntity<String> getMe() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

}