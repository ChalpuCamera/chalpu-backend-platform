package com.example.chalpuplatform.oauth.service;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.OAuth2AuthenticationProcessingException;
import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfo;
import com.example.chalpuplatform.user.domain.User;
import com.example.chalpuplatform.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

/**
 * OAuth2 사용자 조회 및 생성 책임
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2UserResolver {
    
    private final UserRepository userRepository;
    private final OAuth2UserFactory userFactory;
    
    /**
     * 사용자 조회 또는 생성
     */
    @Transactional
    public User resolveUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        Optional<User> userOptional = userRepository.findByEmailWithDeleted(userInfo.getEmail());
        
        if (userOptional.isPresent()) {
            return handleExistingUser(userOptional.get(), userInfo, provider);
        } else {
            return createNewUser(userInfo, provider);
        }
    }
    
    /**
     * 기존 사용자 처리
     */
    private User handleExistingUser(User existingUser, OAuth2UserInfo userInfo, AuthProvider provider) {
        validateProvider(existingUser, provider);
        checkAndHandleDeactivatedUser(existingUser);
        return userFactory.updateUser(existingUser, userInfo);
    }
    
    /**
     * 비활성화된 사용자 확인 및 처리
     */
    private void checkAndHandleDeactivatedUser(User user) {
        if (user.getDeletedAt() != null) {
            log.info("비활성화된 사용자 재활성화: userId={}, email={}", user.getId(), user.getEmail());
            user.activate();
        }
    }
    
    /**
     * 제공자 일치 여부 검증
     */
    private void validateProvider(User user, AuthProvider provider) {
        if (user.getProvider() != null && !user.getProvider().equals(provider)) {
            log.error("OAuth2 제공자 불일치: 기존={}, 요청={}", user.getProvider(), provider);
            throw new OAuth2AuthenticationProcessingException(ErrorMessage.OAUTH_PROVIDER_CONFLICT);
        }
    }
    
    /**
     * 새 사용자 생성
     */
    private User createNewUser(OAuth2UserInfo userInfo, AuthProvider provider) {
        User newUser = userFactory.createUser(userInfo, provider);
        return userRepository.save(newUser);
    }
}