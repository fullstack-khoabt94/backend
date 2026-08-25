package com.eazybytes.auth.services;

import com.eazybytes.auth.dtos.LoginDto;
import com.eazybytes.auth.dtos.LoginResponse;
import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.user.dtos.CreateUserDto;
import com.eazybytes.user.dtos.UserResponse;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.entity.UserInfo;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationProvider authenticationProvider;

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
    public LoginResponse login(LoginDto loginDto) {
        Authentication authentication = this.authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.email(),
                        loginDto.password()
                )
        );
        if (authentication == null) {
            throw new BadCredentialsException("Bad credentials!");
        }

        UserInfo userInfo = (UserInfo) authentication.getDetails();

        if (userInfo == null) {
            throw new BadCredentialsException("Bad credentials!");
        }
        UUID userId = userInfo.getId();

        User fullUser = this.userRepository.findById(userId).orElseThrow();

        // create access token

        return new LoginResponse(UserResponse.fromUser(fullUser));


    }
}