package com.eazybytes.resetpwToken.services;

import com.eazybytes.resetpwToken.dtos.ResetpwTokenRequestDto;
import com.eazybytes.resetpwToken.entity.ResetpwToken;

import java.util.UUID;

public interface ResetpwTokenService {
    void createResetpwToken(ResetpwTokenRequestDto resetpwTokenRequestDto);

    ResetpwToken getValidResetpwToken(String resetpwToken);

    void revokeAllByUserId(UUID userId);

}