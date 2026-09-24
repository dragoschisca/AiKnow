package com.aiknow.chat;

import com.aiknow.ai.dto.RawCitation;
import com.aiknow.search.dto.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Guards against hallucinated citations: an AI-produced citation is only accepted if its
 * chunkId matches a chunk that was physically retrieved for this query. Document name, page
 * number and quote snippet are always taken from the retrieved chunk (ground truth), never
 * trusted from the AI's own output, since the model could otherwise fabricate metadata even
 * for a real chunkId.
 */
@Slf4j
@Component
public class CitationValidator {

    private static final int SNIPPET_MAX_LENGTH = 300;

    public List<MessageCitation> validate(UUID messageId, List<RawCitation> rawCitations, List<SearchResult> retrievedChunks) {
        if (rawCitations == null || rawCitations.isEmpty()) {
            return List.of();
        }

        Map<UUID, SearchResult> retrievedById = new LinkedHashMap<>();
        for (SearchResult chunk : retrievedChunks) {
            retrievedById.put(chunk.chunkId(), chunk);
        }

        Map<UUID, MessageCitation> validated = new LinkedHashMap<>();
        for (RawCitation raw : rawCitations) {
            UUID chunkId = parseChunkId(raw.chunkId());
            if (chunkId == null) {
                log.warn("Discarding citation with unparseable chunkId: {}", raw.chunkId());
                continue;
            }

            SearchResult retrieved = retrievedById.get(chunkId);
            if (retrieved == null) {
                log.warn("Discarding hallucinated citation referencing chunkId {} not in retrieved context", chunkId);
                continue;
            }

            if (validated.containsKey(chunkId)) {
                continue;
            }

            validated.put(chunkId, MessageCitation.builder()
                    .messageId(messageId)
                    .documentId(retrieved.documentId())
                    .documentName(retrieved.documentName())
                    .pageNumber(retrieved.pageNumber())
                    .chunkId(retrieved.chunkId())
                    .quoteSnippet(buildQuoteSnippet(retrieved.content()))
                    .build());
        }

        return new ArrayList<>(validated.values());
    }

    private UUID parseChunkId(String rawChunkId) {
        if (rawChunkId == null || rawChunkId.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawChunkId.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String buildQuoteSnippet(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > SNIPPET_MAX_LENGTH ? content.substring(0, SNIPPET_MAX_LENGTH) + "..." : content;
    }
}
