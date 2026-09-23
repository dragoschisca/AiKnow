package com.aiknow.search;

import com.aiknow.ai.EmbeddingService;
import com.aiknow.organization.OrganizationService;
import com.aiknow.search.dto.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class VectorSearchService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingService embeddingService;
    private final OrganizationService organizationService;

    public List<SearchResult> search(UUID orgId, UUID workspaceId, UUID userId, String query, int topK, double minSimilarity) {
        // Validate membership
        organizationService.validateMembership(orgId, userId);

        float[] queryVector = embeddingService.generateEmbedding(query);
        com.pgvector.PGvector pgVector = new com.pgvector.PGvector(queryVector);

        String sql = """
            SELECT c.id as chunk_id, c.document_id, d.name as document_name, c.page_number, c.content,
                   1 - (c.embedding <=> ?::vector) AS similarity
            FROM document_chunks c
            JOIN documents d ON c.document_id = d.id
            WHERE c.organization_id = ? AND c.workspace_id = ?
              AND d.status = 'READY'
              AND 1 - (c.embedding <=> ?::vector) >= ?
            ORDER BY similarity DESC
            LIMIT ?
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new SearchResult(
                rs.getObject("chunk_id", UUID.class),
                rs.getObject("document_id", UUID.class),
                rs.getString("document_name"),
                rs.getInt("page_number"),
                rs.getString("content"),
                rs.getDouble("similarity")
        ), pgVector, orgId, workspaceId, pgVector, minSimilarity, topK);
    }
}
