package com.eazybytes.auth.services;

import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse signup(CreateUserDto createUserDto) {
        // check for email
        String registerEmail = createUserDto.email();
        if (userRepository.findUserByEmail(registerEmail).isPresent()) {
            throw new BadRequestException("Bad Credentials!");
        }

        // password encode
        String hashPassword = passwordEncoder.encode(createUserDto.password());

        // save user and return
        User newUser = new User();
        newUser.setEmail(registerEmail);
        newUser.setName(createUserDto.name());
        newUser.setPassword(hashPassword);

        User saved = userRepository.save(newUser);
        return UserResponse.fromUser(saved);
    }

    @Override
    public void login() {

    }
}