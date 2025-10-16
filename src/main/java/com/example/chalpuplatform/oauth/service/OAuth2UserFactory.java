package com.example.chalpuplatform.oauth.service;

import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfo;
import com.example.chalpuplatform.user.domain.Role;
import com.example.chalpuplatform.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OAuth2 사용자 생성 및 업데이트 팩토리
 */
@Slf4j
@Component
public class OAuth2UserFactory {

    /**
     * 새로운 OAuth2 사용자 생성 (기본 역할)
     */
    public User createUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        return createUser(userInfo, provider, Role.ROLE_CUSTOMER);
    }

    /**
     * 새로운 OAuth2 사용자 생성 (역할 지정)
     */
    public User createUser(OAuth2UserInfo userInfo, AuthProvider provider, Role role) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .picture(userInfo.getImageUrl())
                .socialId(userInfo.getId())
                .provider(provider)
                .role(role)
                .isActive(true)
                .build();

        log.info("새 OAuth2 사용자 생성: email={}, role={}", user.getEmail(), role);
        return user;
    }

    /**
     * 기존 사용자 정보 업데이트
     */
    public User updateUser(User user, OAuth2UserInfo userInfo) {
        user.updateOAuth2Info(userInfo.getName(), userInfo.getImageUrl());
        log.info("기존 OAuth2 사용자 정보 업데이트: {}", user.getEmail());
        return user;
    }
}