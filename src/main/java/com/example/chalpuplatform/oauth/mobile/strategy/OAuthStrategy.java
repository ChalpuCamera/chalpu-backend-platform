package com.example.chalpuplatform.oauth.mobile.strategy;

import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.oauth.provider.OAuth2UserInfo;

/**
 * OAuth 인증 전략 인터페이스
 * Strategy 패턴을 사용하여 각 OAuth Provider별 인증 로직을 캡슐화
 */
public interface OAuthStrategy {

    /**
     * OAuth 토큰을 검증하고 사용자 정보를 추출
     * @param token OAuth Provider SDK로부터 받은 토큰
     * @return OAuth2UserInfo 사용자 정보
     */
    OAuth2UserInfo getUserInfo(String token);

    /**
     * 이 전략이 처리하는 OAuth Provider 반환
     * @return AuthProvider
     */
    AuthProvider getProvider();

    /**
     * 토큰 유효성 검증
     * @param token 검증할 토큰
     */
    void validateToken(String token);
}
