package com.aiknow.ai.dto;

public record ContextChunk(
        String chunkId,
        String documentName,
        Integer pageNumber,
        String content
) {
}
