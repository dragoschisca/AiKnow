package com.aiknow.chat;

import com.aiknow.ai.AiChatService;
import com.aiknow.ai.dto.ChatCompletionResult;
import com.aiknow.ai.dto.ContextChunk;
import com.aiknow.ai.dto.ConversationTurn;
import com.aiknow.audit.AuditEventPublisher;
import com.aiknow.audit.AuditEventType;
import com.aiknow.chat.dto.ChatMessageResponse;
import com.aiknow.chat.dto.CitationResponse;
import com.aiknow.chat.dto.SendMessageResponse;
import com.aiknow.config.AppProperties;
import com.aiknow.exception.AiServiceException;
import com.aiknow.search.VectorSearchService;
import com.aiknow.search.dto.SearchResult;
import com.aiknow.subscription.UsageService;
import com.aiknow.workspace.Workspace;
import com.aiknow.workspace.WorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates the RAG query flow: retrieve tenant-scoped chunks, ask the AI for a grounded
 * answer, validate its citations against what was actually retrieved, and persist everything
 * atomically.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RagChatService {

    static final String FALLBACK_ANSWER =
            "I couldn't find enough information in the organization's knowledge base to answer this question.";

    private final ConversationService conversationService;
    private final ChatMessageRepository chatMessageRepository;
    private final MessageCitationRepository messageCitationRepository;
    private final VectorSearchService vectorSearchService;
    private final AiChatService aiChatService;
    private final CitationValidator citationValidator;
    private final WorkspaceService workspaceService;
    private final AppProperties appProperties;
    private final UsageService usageService;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public SendMessageResponse sendMessage(UUID conversationId, UUID userId, String content) {
        Conversation conversation = conversationService.getConversationForAccess(conversationId, userId);
        Workspace workspace = workspaceService.getWorkspaceById(conversation.getWorkspaceId());

        usageService.checkAndIncrementQuestionUsage(workspace.getOrganizationId());

        if (conversation.getTitle() == null) {
            conversation.setTitle(ConversationService.deriveTitle(content));
        }

        ChatMessage userMessage = chatMessageRepository.save(ChatMessage.builder()
                .conversationId(conversation.getId())
                .senderType(ChatSenderType.USER)
                .content(content)
                .insufficientInformation(false)
                .build());

        List<ConversationTurn> history = loadHistory(conversation.getId());

        List<SearchResult> retrievedChunks = vectorSearchService.search(
                workspace.getOrganizationId(),
                conversation.getWorkspaceId(),
                userId,
                content,
                appProperties.getRag().getTopK(),
                appProperties.getRag().getMinSimilarity());

        ChatCompletionResult result;
        if (retrievedChunks.isEmpty()) {
            result = new ChatCompletionResult(FALLBACK_ANSWER, List.of(), true);
        } else {
            List<ContextChunk> contextChunks = retrievedChunks.stream()
                    .map(chunk -> new ContextChunk(
                            chunk.chunkId().toString(),
                            chunk.documentName(),
                            chunk.pageNumber(),
                            chunk.content()))
                    .toList();
            try {
                result = aiChatService.generateAnswer(content, contextChunks, history);
            } catch (RuntimeException e) {
                log.error("AI chat completion failed for conversation {}", conversation.getId(), e);
                throw new AiServiceException("Failed to generate an answer from the AI provider", e);
            }
        }

        boolean insufficientInformation = result.insufficientInformation();
        String answer = insufficientInformation ? FALLBACK_ANSWER : result.answer();

        ChatMessage assistantMessage = chatMessageRepository.save(ChatMessage.builder()
                .conversationId(conversation.getId())
                .senderType(ChatSenderType.ASSISTANT)
                .content(answer)
                .insufficientInformation(insufficientInformation)
                .build());

        List<MessageCitation> citations = insufficientInformation
                ? List.of()
                : citationValidator.validate(assistantMessage.getId(), result.citations(), retrievedChunks);
        if (!citations.isEmpty()) {
            messageCitationRepository.saveAll(citations);
        }

        ChatMessageResponse userResponse = ChatMessageResponse.from(userMessage, List.of());
        ChatMessageResponse assistantResponse = ChatMessageResponse.from(
                assistantMessage,
                citations.stream().map(CitationResponse::from).toList());

        auditEventPublisher.publish(AuditEventType.QUESTION_ASKED, workspace.getOrganizationId(), workspace.getId(), userId,
                "CONVERSATION", conversation.getId(),
                Map.of("insufficientInformation", insufficientInformation, "citationCount", citations.size()));

        return new SendMessageResponse(userResponse, assistantResponse);
    }

    private List<ConversationTurn> loadHistory(UUID conversationId) {
        int maxMessages = appProperties.getRag().getMaxHistoryMessages();
        if (maxMessages <= 0) {
            return List.of();
        }

        List<ChatMessage> recent = chatMessageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(0, maxMessages));
        Collections.reverse(recent);

        List<ConversationTurn> turns = new ArrayList<>();
        for (ChatMessage message : recent) {
            ConversationTurn.Role role = message.getSenderType() == ChatSenderType.USER
                    ? ConversationTurn.Role.USER
                    : ConversationTurn.Role.ASSISTANT;
            turns.add(new ConversationTurn(role, message.getContent()));
        }
        return turns;
    }
}
