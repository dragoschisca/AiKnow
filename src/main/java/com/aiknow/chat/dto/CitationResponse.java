package com.aiknow.chat.dto;

import com.aiknow.chat.MessageCitation;

import java.util.UUID;

public record CitationResponse(
        UUID documentId,
        String documentName,
        Integer pageNumber,
        UUID chunkId,
        String quoteSnippet
) {
    public static CitationResponse from(MessageCitation citation) {
        return new CitationResponse(
                citation.getDocumentId(),
                citation.getDocumentName(),
                citation.getPageNumber(),
                citation.getChunkId(),
                citation.getQuoteSnippet()
        );
    }
}
