package com.br.lottus.mobile.auth.service;

import com.br.lottus.mobile.auth.entity.RefreshToken;
import com.br.lottus.mobile.auth.repository.RefreshTokenRepository;
import com.br.lottus.mobile.common.exception.BusinessException;
import com.br.lottus.mobile.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Transactional
    public String createRefreshToken(Usuario usuario) {
        refreshTokenRepository.revokeAllByUsuarioId(usuario.getId());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .usuario(usuario)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created for user: {}", usuario.getEmail());

        return rawToken;
    }

    @Transactional
    public RefreshToken validateAndRotate(String rawToken) {
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException("Refresh token invalido", HttpStatus.UNAUTHORIZED));

        if (!refreshToken.isUsable()) {
            refreshTokenRepository.revokeAllByUsuarioId(refreshToken.getUsuario().getId());
            log.warn("Reuse attempt detected for user: {}", refreshToken.getUsuario().getEmail());
            throw new BusinessException("Refresh token expirado ou revogado", HttpStatus.UNAUTHORIZED);
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return refreshToken;
    }

    @Transactional
    public void revokeAllTokens(Long userId) {
        refreshTokenRepository.revokeAllByUsuarioId(userId);
        log.debug("All refresh tokens revoked for userId: {}", userId);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevoked(Instant.now());
        log.debug("Expired and revoked tokens cleaned up");
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[64];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
