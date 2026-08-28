package com.eazybytes.auth.services;

import com.eazybytes.auth.dtos.LoginDto;
import com.eazybytes.auth.dtos.LoginResponse;
import com.eazybytes.auth.jwt.JwtUtils;
import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.refreshToken.dtos.RefreshTokenDto;
import com.eazybytes.refreshToken.dtos.RefreshTokenResponse;
import com.eazybytes.refreshToken.entity.RefreshToken;
import com.eazybytes.refreshToken.services.RefreshTokenService;
import com.eazybytes.resetpwToken.dtos.ResetPasswordRequestDto;
import com.eazybytes.resetpwToken.entity.ResetpwToken;
import com.eazybytes.resetpwToken.services.ResetpwTokenService;
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
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final ResetpwTokenService resetpwTokenService;

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

        UserInfo userInfo = (UserInfo) authentication.getPrincipal();

        if (userInfo == null) {
            throw new BadCredentialsException("Bad credentials!");
        }
        UUID userId = userInfo.getId();

        User fullUser = this.userRepository.findById(userId).orElseThrow();

        String accessToken = this.jwtUtils.generateToken(authentication);

        RefreshTokenResponse refreshTokenResponse = this.refreshTokenService.createRefreshToken(userId);

        return new LoginResponse(UserResponse.fromUser(fullUser), accessToken, refreshTokenResponse.refreshToken());
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenDto refreshTokenDto) throws BadCredentialsException {
        String refreshToken = refreshTokenDto.refreshToken();
        RefreshToken refreshTokenInfo = this.refreshTokenService.getValidRefreshToken(refreshToken);

        String accessToken = this.jwtUtils.generateToken(new UsernamePasswordAuthenticationToken(
                UserInfo.fromUser(refreshTokenInfo.getUser()),
                null,
                null
        ));

        return new LoginResponse(UserResponse.fromUser(refreshTokenInfo.getUser()), accessToken, refreshToken);
    }

    @Override
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequestDto) {
        // check valid resetpw token
        ResetpwToken resetpwToken = this.resetpwTokenService.getValidResetpwToken(resetPasswordRequestDto.resetpwToken());

        // retrieve user
        User user = resetpwToken.getUser();

        // revoke resetpw token
        this.resetpwTokenService.revokeAllByUserId(user.getId());

        // compare
        if (this.passwordEncoder.matches(resetPasswordRequestDto.newPassword(), user.getPassword())) {
            throw new BadRequestException("Old and new password have to be different");
        }
        String hashedNewPassword = this.passwordEncoder.encode(resetPasswordRequestDto.newPassword());

        // save
        user.setPassword(hashedNewPassword);

        userRepository.save(user);

        // revoke refresh tokens
        this.refreshTokenService.revokeAllByUserId(user.getId());
    }
}