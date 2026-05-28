package com.host.SpringBootAutomationProduction.service;

import com.host.SpringBootAutomationProduction.exceptions.RefreshTokenException;
import com.host.SpringBootAutomationProduction.model.postgres.RefreshToken;
import com.host.SpringBootAutomationProduction.model.postgres.User;
import com.host.SpringBootAutomationProduction.repositories.postgres.RefreshTokenRepository;
import com.host.SpringBootAutomationProduction.repositories.postgres.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Value("${jwt.refresh-token-expiration-days:30}")
    private int refreshTokenExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RefreshTokenException("User not found: " + username));

        refreshTokenRepository.deleteByUserId(user.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(java.util.UUID.randomUUID().toString());
        refreshToken.setUserId(user.getId());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(100));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(java.time.Instant.now())) {
            throw new RefreshTokenException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
            log.info("Refresh token revoked: {}", token);
        });
    }

    @Transactional
    public void revokeAllUserTokens(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        refreshTokenRepository.deleteByUserId(user.getId());
        log.info("All refresh tokens revoked for user: {}", username);
    }

    public boolean hasValidRefreshToken(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        return refreshTokenRepository.findByUserId(user.getId())
                .map(rt -> !rt.isRevoked() && rt.getExpiryDate().isAfter(java.time.Instant.now()))
                .orElse(false);
    }
}