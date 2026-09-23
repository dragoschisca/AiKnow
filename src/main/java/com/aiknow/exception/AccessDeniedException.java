package com.aiknow.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {

    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED);
    }
}
