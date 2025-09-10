package com.example.chalpuplatform.oauth.service;

import com.example.chalpuplatform.common.exception.AuthException;
import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.UserException;
import com.example.chalpuplatform.oauth.dto.AccessTokenDTO;
import com.example.chalpuplatform.oauth.dto.RefreshTokenDTO;
import com.example.chalpuplatform.oauth.security.jwt.JwtTokenProvider;
import com.example.chalpuplatform.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AccessTokenDTO refreshToken(RefreshTokenDTO refreshToken) {
        String oldRefreshToken = refreshToken.getRefreshToken();
        try {
            // 토큰 검증 로직을 AuthService로 이동
            User user = validateAndGetUser(oldRefreshToken);

            String newAccessToken = tokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            
            log.info("event=access_token_refreshed, user_id={}", user.getId());
            return new AccessTokenDTO(newAccessToken);
        } catch (Exception e) {
            log.error("event=access_token_refresh_failed, error_message={}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void logout(Long userId) {
        try {
            refreshTokenService.deleteRefreshTokenByUserId(userId);
            log.info("event=user_logged_out, user_id={}", userId);
        } catch (Exception e) {
            log.error("event=user_logout_failed, user_id={}, error_message={}", userId, e.getMessage(), e);
            throw e;
        }
    }



    // 공통 검증 로직
    private User validateAndGetUser(String refreshToken) {
        if (refreshToken == null) {
            throw new AuthException(ErrorMessage.AUTH_REFRESH_TOKEN_NOT_FOUND);
        }

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new AuthException(ErrorMessage.AUTH_INVALID_REFRESH_TOKEN);
        }

        return refreshTokenService.getUserByRefreshToken(refreshToken)
                .orElseThrow(() -> new UserException(ErrorMessage.USER_NOT_FOUND));
    }
}

