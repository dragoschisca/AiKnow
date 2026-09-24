package com.aiknow.chat.dto;

import com.aiknow.chat.Conversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID workspaceId,
        UUID userId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ConversationResponse from(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getWorkspaceId(),
                conversation.getUserId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt()
        );
    }
}
