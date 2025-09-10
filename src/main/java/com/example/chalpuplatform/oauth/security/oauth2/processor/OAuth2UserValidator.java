package com.example.chalpuplatform.oauth.security.oauth2.processor;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.OAuth2AuthenticationProcessingException;
import com.example.chalpuplatform.oauth.security.oauth2.user.OAuth2UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * OAuth2 사용자 정보 검증 책임
 */
@Slf4j
@Component
public class OAuth2UserValidator {
    
    /**
     * OAuth2 사용자 정보 검증
     */
    public void validate(OAuth2UserInfo oAuth2UserInfo) {
        validateEmail(oAuth2UserInfo);
    }
    
    /**
     * 이메일 유효성 검증
     */
    private void validateEmail(OAuth2UserInfo oAuth2UserInfo) {
        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            log.error("OAuth2 인증 실패: 이메일 정보 없음");
            throw new OAuth2AuthenticationProcessingException(ErrorMessage.OAUTH_EMAIL_NOT_FOUND);
        }
        
        log.debug("OAuth2 이메일 검증 성공: {}", oAuth2UserInfo.getEmail());
    }
}