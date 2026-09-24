package com.aiknow.ai;

import com.aiknow.ai.dto.ChatCompletionResult;
import com.aiknow.ai.dto.ContextChunk;
import com.aiknow.ai.dto.ConversationTurn;

import java.util.List;

/**
 * Provider-agnostic chat/RAG completion contract. Implementations must never leak
 * provider SDK types outside the {@code com.aiknow.ai} package.
 */
public interface AiChatService {

    /**
     * Generates a grounded answer strictly from the supplied context chunks.
     * Implementations must instruct the underlying model to refuse to answer
     * (insufficientInformation = true) when the context is not sufficient, and
     * must never be asked to invent citations beyond the given context.
     */
    ChatCompletionResult generateAnswer(String question, List<ContextChunk> context, List<ConversationTurn> history);
}
