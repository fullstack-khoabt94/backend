package com.eazybytes.resetpwToken.services;

import com.eazybytes.resetpwToken.config.ResetpwTokenProperties;
import com.eazybytes.resetpwToken.dtos.ResetpwTokenRequestDto;
import com.eazybytes.resetpwToken.entity.ResetpwToken;
import com.eazybytes.resetpwToken.repository.ResetpwTokenRepository;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetpwTokenServiceImpl implements ResetpwTokenService {

    private final ResetpwTokenRepository resetpwTokenRepository;
    private final UserRepository userRepository;
    private final ResetpwTokenProperties resetpwTokenProperties;

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    @Override
    public void createResetpwToken(ResetpwTokenRequestDto resetpwTokenRequestDto) {
        User user =
                this.userRepository.findUserByEmail(resetpwTokenRequestDto.email()).orElseThrow(
                        () -> new BadCredentialsException("Invalid email!")
                );

        this.revokeAllByUserId(user.getId());
        ResetpwToken resetpwToken = new ResetpwToken();
        resetpwToken.setUser(user);

        LocalDateTime expiredAt = LocalDateTime.now().plus(
                Duration.ofMillis(this.resetpwTokenProperties.expirationMs())
        );

        String newToken = this.generateToken();
        String hashedNewToken = this.hashToken(newToken);

        resetpwToken.setResetpwToken(hashedNewToken);

        resetpwToken.setExpiredAt(expiredAt);

        this.resetpwTokenRepository.save(resetpwToken);
        log.info("New reset password token issued {}", newToken);
    }

    @Override
    public ResetpwToken getValidResetpwToken(String resetpwToken) {
        String hashedNewToken = this.hashToken(resetpwToken);
        ResetpwToken found = this.resetpwTokenRepository.findResetpwTokenByResetpwToken(hashedNewToken)
                .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        if (found.getIsRevoked() || LocalDateTime.now().isAfter(found.getExpiredAt())) {
            throw new BadCredentialsException("Bad credentials");
        }
        return found;
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        List<ResetpwToken> resetpwTokens = this.resetpwTokenRepository.findByUserId(userId);
        resetpwTokens.forEach(token -> {
            token.setIsRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
        });

        resetpwTokenRepository.saveAll(resetpwTokens);
    }
}