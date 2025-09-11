package com.example.chalpuplatform.oauth.service;

import com.example.chalpuplatform.oauth.service.OAuth2UserProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * 커스텀 OAuth2 사용자 서비스
 * OAuth2 로그인 프로세스의 진입점
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuth2UserProcessor processor;

    /**
     * OAuth2 사용자 정보 로드
     * Spring Security OAuth2 인증 프로세스에서 호출
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        log.info("OAuth2 로그인 시작: provider={}", 
                oAuth2UserRequest.getClientRegistration().getRegistrationId());
        
        // 기본 OAuth2User 정보 로드
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        // 비즈니스 로직 처리를 processor에 위임
        return processor.process(oAuth2UserRequest, oAuth2User);
    }
}