package com.aiknow.chat.dto;

import com.aiknow.chat.ChatMessage;
import com.aiknow.chat.ChatSenderType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ChatMessageResponse(
        UUID id,
        UUID conversationId,
        ChatSenderType senderType,
        String content,
        boolean insufficientInformation,
        List<CitationResponse> citations,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message, List<CitationResponse> citations) {
        return new ChatMessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderType(),
                message.getContent(),
                message.isInsufficientInformation(),
                citations,
                message.getCreatedAt()
        );
    }
}
