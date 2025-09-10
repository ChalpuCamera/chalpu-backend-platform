package com.example.chalpuplatform.common.exception;

/**
 * 알림 서비스 관련 예외 클래스
 * FCM 및 푸시 알림 전송 과정에서 발생하는 예외를 처리하기 위한 커스텀 예외
 */
public class NotificationException extends BaseException {

    public NotificationException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
