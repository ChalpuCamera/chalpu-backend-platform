package com.example.chalpuplatform.oauth.mobile.service;

import com.example.chalpuplatform.fcm.dto.FCMTokenRequest;
import com.example.chalpuplatform.fcm.service.FCMTokenService;
import com.example.chalpuplatform.oauth.dto.TokenDTO;
import com.example.chalpuplatform.oauth.jwt.JwtTokenProvider;
import com.example.chalpuplatform.oauth.mobile.dto.LoginRequest;
import com.example.chalpuplatform.oauth.mobile.dto.LoginResponse;
import com.example.chalpuplatform.oauth.mobile.strategy.OAuthStrategy;
import com.example.chalpuplatform.oauth.mobile.strategy.OAuthStrategyFactory;
import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfo;
import com.example.chalpuplatform.oauth.service.OAuth2UserResolver;
import com.example.chalpuplatform.oauth.service.RefreshTokenService;
import com.example.chalpuplatform.user.domain.Role;
import com.example.chalpuplatform.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MobileOAuthService {

    private final OAuthStrategyFactory strategyFactory;
    private final OAuth2UserResolver oauth2UserResolver;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final FCMTokenService fcmTokenService;

    @Transactional
    public LoginResponse processOAuthLogin(AuthProvider provider, LoginRequest request) {
        log.info("Processing mobile OAuth login: provider={}, userType={}", provider, request.getUserType());

        // 1. Strategy 선택
        OAuthStrategy strategy = strategyFactory.getStrategy(provider);

        // 2. 토큰 검증 및 UserInfo 추출
        OAuth2UserInfo userInfo = strategy.getUserInfo(request.getAccessToken());

        // 3. userType에 따라 Role 결정
        Role role = determineRole(request.getUserType());

        // 4. 사용자 조회 또는 생성
        User user = oauth2UserResolver.resolveUser(userInfo, provider, role);

        // 5. JWT 토큰 생성
        TokenDTO tokens = jwtTokenProvider.generateTokens(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        // 6. Refresh Token 저장
        refreshTokenService.saveRefreshToken(tokens.getRefreshToken(), user.getId());

        // 7. FCM 토큰 등록 (실패해도 로그인 계속)
        registerDeviceInfo(user.getId(), request);

        log.info("Mobile OAuth login successful: userId={}, provider={}, role={}",
                user.getId(), provider, user.getRole());

        return new LoginResponse(tokens, user.getId());
    }

    private Role determineRole(String userType) {
        if ("owner".equalsIgnoreCase(userType)) {
            return Role.ROLE_OWNER;
        } else if ("customer".equalsIgnoreCase(userType)) {
            return Role.ROLE_CUSTOMER;
        }

        log.warn("Unknown userType: {}, defaulting to ROLE_CUSTOMER", userType);
        return Role.ROLE_CUSTOMER;
    }

    private void registerDeviceInfo(Long userId, LoginRequest request) {
        try {
            String fcmToken = request.getFcmToken();

            FCMTokenRequest fcmRequest = FCMTokenRequest.builder()
                    .userId(userId)
                    .fcmToken(fcmToken != null && !fcmToken.trim().isEmpty() ? fcmToken : null)
                    .deviceType(request.getDeviceType())
                    .deviceModel(request.getDeviceModel())
                    .appVersion(request.getAppVersion())
                    .osVersion(request.getOsVersion())
                    .isAllowed(request.getIsAllowed())
                    .build();

            fcmTokenService.registerOrUpdateToken(fcmRequest);

            log.info("Device info registered: userId={}, deviceType={}", userId, request.getDeviceType());

        } catch (Exception e) {
            log.warn("Failed to register device info (login continues): userId={}, error={}", userId, e.getMessage());
        }
    }
}
