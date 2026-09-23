package com.aiknow.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(UUID documentId);
    void deleteByDocumentId(UUID documentId);
    long countByDocumentId(UUID documentId);
}
