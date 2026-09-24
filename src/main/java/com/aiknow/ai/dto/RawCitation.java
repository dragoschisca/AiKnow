package com.aiknow.ai.dto;

public record RawCitation(
        String chunkId,
        String documentName,
        Integer page,
        String quoteSnippet
) {
}
