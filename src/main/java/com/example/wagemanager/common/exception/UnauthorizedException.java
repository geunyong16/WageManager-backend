package com.example.wagemanager.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final String errorCode;
    private final String errorMessage;

    public UnauthorizedException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
