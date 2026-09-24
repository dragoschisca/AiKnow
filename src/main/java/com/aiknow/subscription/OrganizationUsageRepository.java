package com.aiknow.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationUsageRepository extends JpaRepository<OrganizationUsage, UUID> {

    Optional<OrganizationUsage> findByOrganizationId(UUID organizationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OrganizationUsage> findWithLockByOrganizationId(UUID organizationId);
}
