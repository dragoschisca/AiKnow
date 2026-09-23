package com.aiknow.document;

import com.aiknow.ai.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final FileStorageService fileStorageService;
    private final PdfDocumentProcessor pdfDocumentProcessor;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    @Async
    public void processDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) {
            log.error("Document not found: {}", documentId);
            return;
        }

        document.setStatus(DocumentStatus.PROCESSING);
        documentRepository.save(document);

        try {
            InputStream inputStream = fileStorageService.load(document.getStoragePath());
            List<TextChunker.PageText> pages = pdfDocumentProcessor.extractPages(inputStream);
            List<TextChunker.ChunkResult> chunkResults = textChunker.chunkText(pages);

            List<DocumentChunk> chunks = chunkResults.stream()
                    .map(result -> DocumentChunk.builder()
                            .documentId(document.getId())
                            .organizationId(document.getOrganizationId())
                            .workspaceId(document.getWorkspaceId())
                            .chunkIndex(result.chunkIndex())
                            .pageNumber(result.pageNumber())
                            .content(result.content())
                            .tokenCount(result.tokenCount())
                            .build())
                    .collect(Collectors.toList());

            // Save chunks first to get their IDs populated
            chunks = documentChunkRepository.saveAll(chunks);

            // Extract text for embeddings
            List<String> texts = chunks.stream()
                    .map(DocumentChunk::getContent)
                    .collect(Collectors.toList());

            // Generate embeddings in batch
            List<float[]> embeddings = embeddingService.generateEmbeddings(texts);

            // Update embeddings directly via JDBC since Hibernate doesn't map it
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = chunks.get(i);
                float[] embedding = embeddings.get(i);
                jdbcTemplate.update(
                        "UPDATE document_chunks SET embedding = ?::vector WHERE id = ?",
                        new com.pgvector.PGvector(embedding),
                        chunk.getId()
                );
            }

            document.setStatus(DocumentStatus.READY);
            documentRepository.save(document);
            log.info("Document processed: {} chunks created and embedded for document {}", chunks.size(), document.getId());
        } catch (Exception e) {
            log.error("Error processing document: {}", document.getId(), e);
            document.setStatus(DocumentStatus.FAILED);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 1000) {
                errorMessage = errorMessage.substring(0, 1000);
            }
            document.setErrorMessage(errorMessage);
            documentRepository.save(document);
        }
    }
}
