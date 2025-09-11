package com.example.chalpuplatform.oauth.strategy;

import com.example.chalpuplatform.user.domain.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * 사용자 타입별 처리 전략 인터페이스
 */
public interface UserTypeStrategy {
    
    /**
     * 사용자 타입에 따른 처리
     * @param user 처리할 사용자
     * @param oAuth2User OAuth2 사용자 정보
     * @return 처리된 OAuth2User
     */
    OAuth2User processUser(User user, OAuth2User oAuth2User);
    
    /**
     * 이 전략이 처리하는 사용자 타입
     * @return 사용자 타입 문자열
     */
    String getUserType();
}