package com.eazybytes.user.services;

import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUser(UUID userId) {
        return this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User"));
    }
}