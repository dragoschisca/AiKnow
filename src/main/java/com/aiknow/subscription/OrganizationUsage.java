package com.aiknow.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organization_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "organization_id", nullable = false, unique = true)
    private UUID organizationId;

    @Builder.Default
    @Column(name = "document_count", nullable = false)
    private Integer documentCount = 0;

    @Builder.Default
    @Column(name = "storage_bytes", nullable = false)
    private Long storageBytes = 0L;

    @Builder.Default
    @Column(name = "questions_this_month", nullable = false)
    private Integer questionsThisMonth = 0;

    @Column(name = "reset_date", nullable = false)
    private LocalDate resetDate;
}
