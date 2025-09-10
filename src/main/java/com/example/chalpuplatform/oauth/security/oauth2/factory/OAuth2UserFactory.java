package com.example.chalpuplatform.oauth.security.oauth2.factory;

import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.security.oauth2.user.OAuth2UserInfo;
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
     * 새로운 OAuth2 사용자 생성
     */
    public User createUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .picture(userInfo.getImageUrl())
                .socialId(userInfo.getId())
                .provider(provider)
                .role(Role.ROLE_USER)  // 기본 역할
                .isActive(true)
                .build();
        
        log.info("새 OAuth2 사용자 생성: {}", user.getEmail());
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