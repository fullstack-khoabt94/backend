package com.eazybytes.user.controllers;

import com.eazybytes.user.services.IUserServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserControllers {

    private final IUserServices userServices;

    @GetMapping("/me")
    public ResponseEntity<String> getMe() {
        this.userServices.getMe();
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

}