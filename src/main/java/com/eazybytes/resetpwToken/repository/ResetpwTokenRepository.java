package com.eazybytes.resetpwToken.repository;

import com.eazybytes.resetpwToken.entity.ResetpwToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResetpwTokenRepository extends JpaRepository<ResetpwToken, UUID> {
    Optional<ResetpwToken> findResetpwTokenByResetpwToken(String resetpwToken);

    List<ResetpwToken> findByUserId(UUID userId);
}