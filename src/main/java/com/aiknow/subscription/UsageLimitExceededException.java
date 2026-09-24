package com.aiknow.subscription;

import com.aiknow.exception.ApiException;
import com.aiknow.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class UsageLimitExceededException extends ApiException {

    public UsageLimitExceededException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS, ErrorCode.USAGE_LIMIT_EXCEEDED);
    }
}
