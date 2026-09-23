package com.aiknow.workspace;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {
    List<Workspace> findByOrganizationId(UUID organizationId);
    Optional<Workspace> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
