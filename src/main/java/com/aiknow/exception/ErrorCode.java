package com.aiknow.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("USR_001"),
    USER_ALREADY_EXISTS("USR_002"),
    INVALID_CREDENTIALS("USR_003"),
    
    ORG_NOT_FOUND("ORG_001"),
    ORG_ALREADY_EXISTS("ORG_002"),
    ORG_MEMBER_EXISTS("ORG_003"),
    
    WORKSPACE_NOT_FOUND("WS_001"),
    
    DOCUMENT_NOT_FOUND("DOC_001"),
    DOCUMENT_PROCESSING_FAILED("DOC_002"),
    INVALID_FILE_TYPE("DOC_003"),
    
    CONVERSATION_NOT_FOUND("CONV_001"),

    AI_SERVICE_ERROR("AI_001"),

    USAGE_LIMIT_EXCEEDED("USG_001"),

    ACCESS_DENIED("SEC_001"),
    INVALID_TOKEN("SEC_002"),
    TOKEN_EXPIRED("SEC_003"),
    
    INTERNAL_ERROR("SYS_001"),
    VALIDATION_ERROR("SYS_002");

    private final String code;
}
