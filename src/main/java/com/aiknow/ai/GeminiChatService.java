package com.aiknow.ai;

import com.aiknow.ai.dto.ChatCompletionResult;
import com.aiknow.ai.dto.ContextChunk;
import com.aiknow.ai.dto.ConversationTurn;
import com.aiknow.config.AppProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiChatService implements AiChatService {

    private static final String FALLBACK_ANSWER =
            "I couldn't find enough information in the organization's knowledge base to answer this question.";

    private static final String SYSTEM_INSTRUCTION = """
            You are AiKnow, an enterprise knowledge assistant for an internal organizational knowledge base.

            STRICT RULES (non-negotiable):
            1. Answer ONLY using facts explicitly present in the numbered context snippets provided in the user's message.
            2. NEVER use outside knowledge, training data, or assumptions. NEVER extrapolate, infer, or invent facts,
               procedures, names, dates, numbers, or policies that are not literally stated in the provided snippets.
            3. Every factual claim in your answer must be traceable to at least one provided snippet. For each claim,
               add a citation with the exact "chunkId" of the snippet it came from, copied character-for-character from
               the snippet header.
            4. If the snippets do not contain enough information to answer the question fully and accurately, you MUST
               set "insufficientInformation" to true and set "answer" to exactly:
               "I couldn't find enough information in the organization's knowledge base to answer this question."
               In that case return an empty "citations" array.
            5. Never fabricate a chunkId. Only use chunkId values that appear verbatim in the provided snippets.
            6. Respond with JSON only, matching the provided response schema exactly. Do not include markdown,
               commentary, or text outside the JSON object.
            """;

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private Client client;
    private String model;
    private Schema responseSchema;

    @PostConstruct
    public void init() {
        this.client = Client.builder().apiKey(appProperties.getGemini().getApiKey()).build();
        this.model = appProperties.getGemini().getChatModel();
        this.responseSchema = buildResponseSchema();
    }

    @Override
    public ChatCompletionResult generateAnswer(String question, List<ContextChunk> context, List<ConversationTurn> history) {
        if (context == null || context.isEmpty()) {
            return new ChatCompletionResult(FALLBACK_ANSWER, List.of(), true);
        }

        List<Content> contents = buildContents(question, context, history);
        GenerateContentConfig config = GenerateContentConfig.builder()
                .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_INSTRUCTION)))
                .responseMimeType("application/json")
                .responseSchema(responseSchema)
                .temperature(0.1f)
                .build();

        String json = callWithRetry(contents, config);
        return parseResponse(json);
    }

    private List<Content> buildContents(String question, List<ContextChunk> context, List<ConversationTurn> history) {
        List<Content> contents = new ArrayList<>();

        if (history != null) {
            for (ConversationTurn turn : history) {
                String role = turn.role() == ConversationTurn.Role.USER ? "user" : "model";
                contents.add(Content.builder().role(role).parts(Part.fromText(turn.content())).build());
            }
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("CONTEXT SNIPPETS:\n\n");
        for (int i = 0; i < context.size(); i++) {
            ContextChunk chunk = context.get(i);
            prompt.append("[Snippet ").append(i + 1).append("]\n")
                    .append("chunkId: ").append(chunk.chunkId()).append("\n")
                    .append("document: ").append(chunk.documentName()).append("\n")
                    .append("page: ").append(chunk.pageNumber()).append("\n")
                    .append("content: ").append(chunk.content()).append("\n\n");
        }
        prompt.append("QUESTION:\n").append(question);

        contents.add(Content.builder().role("user").parts(Part.fromText(prompt.toString())).build());
        return contents;
    }

    private Schema buildResponseSchema() {
        Schema citationSchema = Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "chunkId", Schema.builder().type(Type.Known.STRING).build(),
                        "documentName", Schema.builder().type(Type.Known.STRING).build(),
                        "page", Schema.builder().type(Type.Known.INTEGER).build(),
                        "quoteSnippet", Schema.builder().type(Type.Known.STRING).build()
                ))
                .required(List.of("chunkId"))
                .build();

        return Schema.builder()
                .type(Type.Known.OBJECT)
                .properties(Map.of(
                        "answer", Schema.builder().type(Type.Known.STRING).build(),
                        "insufficientInformation", Schema.builder().type(Type.Known.BOOLEAN).build(),
                        "citations", Schema.builder().type(Type.Known.ARRAY).items(citationSchema).build()
                ))
                .required(List.of("answer", "insufficientInformation", "citations"))
                .build();
    }

    private String callWithRetry(List<Content> contents, GenerateContentConfig config) {
        int maxRetries = appProperties.getGemini().getMaxRetries();
        long backoffMs = appProperties.getGemini().getInitialBackoffMs();

        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                GenerateContentResponse response = client.models.generateContent(model, contents, config);
                return response.text();
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == maxRetries) {
                    break;
                }
                long delay = backoffMs * (1L << attempt);
                log.warn("Gemini chat call failed (attempt {}/{}), retrying in {}ms: {}",
                        attempt + 1, maxRetries + 1, delay, e.getMessage());
                sleep(delay);
            }
        }
        log.error("Failed to generate chat completion after {} attempts", maxRetries + 1, lastError);
        throw new RuntimeException("Failed to generate chat completion after " + (maxRetries + 1) + " attempts", lastError);
    }

    private ChatCompletionResult parseResponse(String json) {
        try {
            return objectMapper.readValue(json, ChatCompletionResult.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini structured JSON response: {}", json, e);
            return new ChatCompletionResult(FALLBACK_ANSWER, List.of(), true);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while retrying Gemini chat call", ie);
        }
    }
}
