package com.example.chalpuplatform.common.exception;

public class RefreshTokenException extends BaseException {
    public RefreshTokenException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
