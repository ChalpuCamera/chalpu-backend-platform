package com.example.chalpuplatform.common.exception;

/**
 * FCM 관련 예외 클래스
 * FCM 토큰 관리 및 알림 전송 관련 예외를 처리합니다.
 * 
 * @author Backend Team
 * @version 1.0
 * @since 2025-06-09
 */
public class FCMException extends BaseException {
    
    public FCMException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
