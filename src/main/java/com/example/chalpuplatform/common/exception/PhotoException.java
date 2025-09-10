package com.example.chalpuplatform.common.exception;

/**
 * 사진 관련 예외
 */
public class PhotoException extends BaseException {
    
    public PhotoException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
} 