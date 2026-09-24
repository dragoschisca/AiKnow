package com.aiknow.chat.dto;

public record SendMessageResponse(
        ChatMessageResponse userMessage,
        ChatMessageResponse assistantMessage
) {
}
