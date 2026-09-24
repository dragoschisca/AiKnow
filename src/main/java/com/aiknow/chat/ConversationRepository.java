package com.aiknow.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByWorkspaceIdOrderByUpdatedAtDesc(UUID workspaceId);
    Optional<Conversation> findByIdAndWorkspaceId(UUID id, UUID workspaceId);
}
