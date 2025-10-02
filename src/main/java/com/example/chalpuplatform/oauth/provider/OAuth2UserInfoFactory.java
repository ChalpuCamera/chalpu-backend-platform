package com.example.chalpuplatform.oauth.provider;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.OAuthException;
import com.example.chalpuplatform.oauth.model.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(AuthProvider authProvider, Map<String, Object> attributes) {
        switch (authProvider) {
            case KAKAO:
                return new KakaoOAuth2UserInfo(attributes);
            case GOOGLE:
                return new GoogleOAuth2UserInfo(attributes);
            default:
                throw new OAuthException(ErrorMessage.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
    }

    // 기존 메서드도 유지 (하위 호환성)
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        try {
            AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
            return getOAuth2UserInfo(authProvider, attributes);
        } catch (IllegalArgumentException e) {
            throw new OAuthException(ErrorMessage.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
    }
}
