package com.example.chalpuplatform.common.exception;

/**
 * OAuth2 인증 처리 예외
 */
public class OAuth2AuthenticationProcessingException extends BaseException {
    
    public OAuth2AuthenticationProcessingException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
