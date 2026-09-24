package com.aiknow.chat;

import com.aiknow.chat.dto.ChatMessageResponse;
import com.aiknow.chat.dto.SendMessageRequest;
import com.aiknow.chat.dto.SendMessageResponse;
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

/**
 * Flat conversation-scoped chat endpoints, mirroring the RAG query flow: a conversation
 * carries its own workspace/tenant context, so callers only need the conversation id.
 */
@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/messages")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final RagChatService ragChatService;
    private final ConversationService conversationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SendMessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        UUID userId = getCurrentUserId();
        SendMessageResponse response = ragChatService.sendMessage(conversationId, userId, request.content());
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<ChatMessageResponse>> listMessages(@PathVariable UUID conversationId) {
        UUID userId = getCurrentUserId();
        return ApiResponse.success(conversationService.getMessages(conversationId, userId));
    }

    private UUID getCurrentUserId() {
        UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.getId();
    }
}
