package com.aiknow.ai.dto;

public record ConversationTurn(
        Role role,
        String content
) {
    public enum Role {
        USER,
        ASSISTANT
    }
}
