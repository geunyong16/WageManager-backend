package com.example.wagemanager.domain.auth.service;

import com.example.wagemanager.common.exception.ErrorCode;
import com.example.wagemanager.common.exception.NotFoundException;
import com.example.wagemanager.common.exception.UnauthorizedException;
import com.example.wagemanager.domain.auth.entity.RefreshToken;
import com.example.wagemanager.domain.auth.repository.RefreshTokenRepository;
import com.example.wagemanager.global.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * JWT 토큰 생성 및 Refresh Token 관리를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 토큰 쌍 (Access Token + Refresh Token)
     */
    @Getter
    @AllArgsConstructor
    @Builder
    public static class TokenPair {
        private String accessToken;
        private String refreshToken;
    }

    /**
     * Access Token 생성
     *
     * @param userId 사용자 ID
     * @return JWT Access Token
     */
    public String generateAccessToken(Long userId) {
        return jwtTokenProvider.generateToken(userId);
    }

    /**
     * Refresh Token 생성 및 DB에 저장 (RTR 방식 - 기존 토큰 폐기 후 신규 생성)
     *
     * @param userId 사용자 ID
     * @return Refresh Token 문자열
     */
    @Transactional
    public String generateAndSaveRefreshToken(Long userId) {
        // 기존 Refresh Token이 있다면 폐기
        refreshTokenRepository.deleteByUserId(userId);

        // 새로운 Refresh Token 생성
        String refreshTokenString = jwtTokenProvider.generateRefreshToken(userId);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshExpirationTime() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .token(refreshTokenString)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshTokenString;
    }

    /**
     * Access Token과 Refresh Token 쌍 생성
     *
     * @param userId 사용자 ID
     * @return 토큰 쌍
     */
    @Transactional
    public TokenPair generateTokenPair(Long userId) {
        String accessToken = generateAccessToken(userId);
        String refreshToken = generateAndSaveRefreshToken(userId);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Refresh Token을 사용하여 새로운 Access Token과 Refresh Token 발급 (RTR 방식)
     * 기존 Refresh Token은 폐기되고 새로운 토큰 쌍이 발급됩니다.
     *
     * @param refreshTokenString Refresh Token 문자열
     * @return 새로운 토큰 쌍
     * @throws UnauthorizedException Refresh Token이 유효하지 않거나 만료된 경우
     * @throws NotFoundException Refresh Token을 찾을 수 없는 경우
     */
    @Transactional
    public TokenPair refreshAccessToken(String refreshTokenString) {
        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshTokenString)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN, "유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new NotFoundException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "Refresh Token을 찾을 수 없습니다."));

        // 만료 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new UnauthorizedException(ErrorCode.EXPIRED_REFRESH_TOKEN, "만료된 Refresh Token입니다. 다시 로그인해주세요.");
        }

        Long userId = refreshToken.getUserId();

        // RTR: 기존 Refresh Token 폐기
        refreshTokenRepository.delete(refreshToken);

        // 새로운 Access Token 및 Refresh Token 생성
        return generateTokenPair(userId);
    }

    /**
     * Refresh Token 삭제 (로그아웃 시 사용)
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void revokeRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
