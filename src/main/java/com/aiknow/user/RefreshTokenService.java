package com.aiknow.user;

import com.aiknow.config.AppProperties;
import com.aiknow.exception.BadRequestException;
import com.aiknow.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppProperties appProperties;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiresAt(Instant.now().plusMillis(appProperties.getJwt().getRefreshExpirationMs()))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Refresh token not found", ErrorCode.INVALID_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token is revoked", ErrorCode.INVALID_TOKEN);
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token expired", ErrorCode.TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
