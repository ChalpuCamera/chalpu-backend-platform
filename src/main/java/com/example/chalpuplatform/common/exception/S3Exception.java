package com.example.chalpuplatform.common.exception;

public class S3Exception extends BaseException {
    public S3Exception(ErrorMessage errorMessage) {
        super(errorMessage);
    }
} 