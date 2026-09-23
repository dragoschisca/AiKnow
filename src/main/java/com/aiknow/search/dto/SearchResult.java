package com.aiknow.search.dto;

import java.util.UUID;

public record SearchResult(
        UUID chunkId,
        UUID documentId,
        String documentName,
        Integer pageNumber,
        String content,
        Double similarity
) {
}
