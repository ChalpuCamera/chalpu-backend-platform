package com.example.chalpuplatform.common.exception;

/**
 * 매장 관련 예외
 */
public class StoreException extends BaseException {
    
    public StoreException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
} 