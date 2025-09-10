package com.example.chalpuplatform.common.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final ErrorMessage errorMessage;
    
    public BaseException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}
