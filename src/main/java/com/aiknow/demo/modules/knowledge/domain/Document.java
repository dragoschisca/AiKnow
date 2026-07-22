package com.aiknow.demo.modules.knowledge.domain;

import com.aiknow.demo.modules.company.domain.Company;
import com.aiknow.demo.modules.knowledge.domain.enums.DocumentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "document_sequence")
    @SequenceGenerator(name = "document_sequence", sequenceName = "document_sequence", allocationSize = 1)
    private Long Id;
    @NotBlank(message = "Numele documentului este obligatoriu")
    private String Name;
    @ManyToOne(optional = false)
    private Company Company;
    @NotBlank(message = "Extensia este obligatorie")
    @Column(name = "extension", nullable = false, length = 10)
    private String extension;
    @NotNull
    @PositiveOrZero
    @Column(name = "size_in_bytes", nullable = false)
    private Long sizeInBytes;
    @Column(name = "storage_path", nullable = false)
    private String storagePath;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DocumentStatus status = DocumentStatus.ACTIVE;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "author_id")
    private Long authorId;

    @PrePersist
    private void prePersist(){
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public Document() {}

    public Document(String name, Company company, String extension, Long sizeInBytes, String storagePath, Long authorId) {
        this.Name = name;
        this.Company = company;
        this.extension = extension;
        this.sizeInBytes = sizeInBytes;
        this.storagePath = storagePath;
        this.authorId = authorId;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Company getCompany() {
        return Company;
    }

    public void setCompany(Company company) {
        Company = company;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAuthor() {
        return authorId;
    }

    public void setAuthor(Long authorId) {
        this.authorId = authorId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(Id, document.Id) && Objects.equals(Name, document.Name) && Objects.equals(Company, document.Company) && Objects.equals(extension, document.extension) && Objects.equals(sizeInBytes, document.sizeInBytes) && Objects.equals(storagePath, document.storagePath) && status == document.status && Objects.equals(createdAt, document.createdAt) && Objects.equals(updatedAt, document.updatedAt) && Objects.equals(authorId, document.authorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, Name, Company, extension, sizeInBytes, storagePath, status, createdAt, updatedAt, authorId);
    }
}
