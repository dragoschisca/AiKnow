package com.aiknow.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends ApiException {

    public AiServiceException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_GATEWAY, ErrorCode.AI_SERVICE_ERROR);
        if (cause != null) {
            initCause(cause);
        }
    }
}
