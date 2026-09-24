package com.aiknow.chat;

import com.aiknow.chat.dto.CitationResponse;
import com.aiknow.chat.dto.ChatMessageResponse;
import com.aiknow.chat.dto.ConversationResponse;
import com.aiknow.exception.ErrorCode;
import com.aiknow.exception.ResourceNotFoundException;
import com.aiknow.organization.OrganizationService;
import com.aiknow.workspace.Workspace;
import com.aiknow.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConversationService {

    private static final int DEFAULT_TITLE_LENGTH = 60;

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageCitationRepository messageCitationRepository;
    private final OrganizationService organizationService;
    private final WorkspaceService workspaceService;

    @Transactional
    public Conversation createConversation(UUID orgId, UUID workspaceId, UUID userId, String title) {
        organizationService.validateMembership(orgId, userId);
        workspaceService.getWorkspace(orgId, workspaceId);

        Conversation conversation = Conversation.builder()
                .workspaceId(workspaceId)
                .userId(userId)
                .title(title)
                .build();
        return conversationRepository.save(conversation);
    }

    public List<ConversationResponse> getConversations(UUID orgId, UUID workspaceId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        workspaceService.getWorkspace(orgId, workspaceId);

        return conversationRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .map(ConversationResponse::from)
                .toList();
    }

    public ConversationResponse getConversation(UUID orgId, UUID workspaceId, UUID conversationId, UUID userId) {
        organizationService.validateMembership(orgId, userId);
        Conversation conversation = conversationRepository.findByIdAndWorkspaceId(conversationId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found", ErrorCode.CONVERSATION_NOT_FOUND));
        return ConversationResponse.from(conversation);
    }

    public List<ChatMessageResponse> getMessages(UUID conversationId, UUID userId) {
        Conversation conversation = getConversationForAccess(conversationId, userId);

        List<ChatMessage> messages = chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        List<UUID> messageIds = messages.stream().map(ChatMessage::getId).toList();
        Map<UUID, List<CitationResponse>> citationsByMessage = messageCitationRepository.findByMessageIdIn(messageIds).stream()
                .collect(Collectors.groupingBy(MessageCitation::getMessageId,
                        Collectors.mapping(CitationResponse::from, Collectors.toList())));

        return messages.stream()
                .map(m -> ChatMessageResponse.from(m, citationsByMessage.getOrDefault(m.getId(), List.of())))
                .toList();
    }

    /**
     * Resolves a conversation and validates the caller belongs to the owning organization,
     * without requiring the organization/workspace ids on the URL (used by the flat
     * {@code /api/v1/conversations/{id}/messages} endpoint).
     */
    Conversation getConversationForAccess(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found", ErrorCode.CONVERSATION_NOT_FOUND));

        Workspace workspace = workspaceService.getWorkspaceById(conversation.getWorkspaceId());
        organizationService.validateMembership(workspace.getOrganizationId(), userId);
        return conversation;
    }

    static String deriveTitle(String firstMessageContent) {
        if (firstMessageContent == null || firstMessageContent.isBlank()) {
            return "New Conversation";
        }
        String trimmed = firstMessageContent.trim();
        return trimmed.length() > DEFAULT_TITLE_LENGTH
                ? trimmed.substring(0, DEFAULT_TITLE_LENGTH) + "..."
                : trimmed;
    }
}
