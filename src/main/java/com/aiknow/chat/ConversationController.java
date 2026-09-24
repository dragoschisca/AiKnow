package com.aiknow.chat;

import com.aiknow.chat.dto.ChatMessageResponse;
import com.aiknow.chat.dto.ConversationResponse;
import com.aiknow.chat.dto.CreateConversationRequest;
import com.aiknow.common.ApiResponse;
import com.aiknow.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{orgId}/workspaces/{workspaceId}/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ConversationResponse> create(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateConversationRequest request) {
        UUID userId = getCurrentUserId();
        Conversation conversation = conversationService.createConversation(orgId, workspaceId, userId, request.title());
        return ApiResponse.success(ConversationResponse.from(conversation));
    }

    @GetMapping
    public ApiResponse<List<ConversationResponse>> list(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId) {
        UUID userId = getCurrentUserId();
        return ApiResponse.success(conversationService.getConversations(orgId, workspaceId, userId));
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<ConversationResponse> get(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId) {
        UUID userId = getCurrentUserId();
        return ApiResponse.success(conversationService.getConversation(orgId, workspaceId, conversationId, userId));
    }

    @GetMapping("/{conversationId}/messages")
    public ApiResponse<List<ChatMessageResponse>> getMessages(
            @PathVariable UUID orgId,
            @PathVariable UUID workspaceId,
            @PathVariable UUID conversationId) {
        UUID userId = getCurrentUserId();
        conversationService.getConversation(orgId, workspaceId, conversationId, userId);
        return ApiResponse.success(conversationService.getMessages(conversationId, userId));
    }

    private UUID getCurrentUserId() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }
}
