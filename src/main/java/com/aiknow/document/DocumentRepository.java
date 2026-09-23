package com.aiknow.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByOrganizationIdAndWorkspaceId(UUID organizationId, UUID workspaceId);
    Optional<Document> findByIdAndOrganizationIdAndWorkspaceId(UUID id, UUID organizationId, UUID workspaceId);
    List<Document> findByOrganizationIdAndWorkspaceIdAndStatus(UUID organizationId, UUID workspaceId, DocumentStatus status);
    long countByOrganizationId(UUID organizationId);
}
