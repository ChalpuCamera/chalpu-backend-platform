package com.example.chalpuplatform.jar.exception;

import com.example.chalpuplatform.common.exception.BaseException;
import com.example.chalpuplatform.common.exception.ErrorMessage;

public class JARException extends BaseException {
    public JARException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}