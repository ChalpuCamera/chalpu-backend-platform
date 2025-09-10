package com.example.chalpuplatform.common.exception;

/**
 * 음식 관련 예외
 */
public class FoodException extends BaseException {
    
    public FoodException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
} 