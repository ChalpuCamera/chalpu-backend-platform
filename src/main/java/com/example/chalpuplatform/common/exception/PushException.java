package com.example.chalpuplatform.common.exception;

/**
 * 푸시 알림 설정 관련 예외
 */
public class PushException extends BaseException {
    
    public PushException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
} 