package com.eazybytes.user.controllers;

import com.eazybytes.user.entity.User;
import com.eazybytes.user.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getMe() {
        User user = this.userService.getByID(UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

}