package com.aiknow.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotBlank(message = "Message content must not be blank")
        @Size(max = 8000, message = "Message content must not exceed 8000 characters")
        String content
) {
}
