package com.aiknow.document;

import com.aiknow.config.AppProperties;
import com.aiknow.exception.ErrorCode;
import com.aiknow.exception.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final AppProperties appProperties;
    private Path rootDir;

    @PostConstruct
    public void init() {
        try {
            this.rootDir = Paths.get(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
            Files.createDirectories(this.rootDir);
            log.info("Initialized local file storage at: {}", this.rootDir);
        } catch (IOException e) {
            log.error("Could not initialize local file storage", e);
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(UUID organizationId, UUID workspaceId, String filename, InputStream inputStream) {
        try {
            String relativePathStr = organizationId.toString() + "/" + workspaceId.toString();
            Path relativePath = Paths.get(relativePathStr);
            Path destinationDir = this.rootDir.resolve(relativePath);
            
            Files.createDirectories(destinationDir);
            
            String uniqueFilename = UUID.randomUUID().toString() + "-" + filename;
            Path destinationFile = destinationDir.resolve(uniqueFilename);
            
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Stored file at: {}", destinationFile);
            
            return relativePathStr + "/" + uniqueFilename;
        } catch (IOException e) {
            log.error("Failed to store file: {}", filename, e);
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    @Override
    public InputStream load(String storagePath) {
        try {
            Path file = this.rootDir.resolve(storagePath).normalize();
            if (!Files.exists(file) || !Files.isReadable(file)) {
                throw new ResourceNotFoundException("File not found or not readable", ErrorCode.DOCUMENT_NOT_FOUND);
            }
            return Files.newInputStream(file);
        } catch (IOException e) {
            log.error("Failed to load file: {}", storagePath, e);
            throw new RuntimeException("Failed to load file " + storagePath, e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path file = this.rootDir.resolve(storagePath).normalize();
            if (Files.exists(file)) {
                Files.delete(file);
                log.debug("Deleted file at: {}", file);
            } else {
                log.warn("File to delete does not exist: {}", file);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", storagePath, e);
            throw new RuntimeException("Failed to delete file " + storagePath, e);
        }
    }

    @Override
    public Path getStorageLocation(String storagePath) {
        return this.rootDir.resolve(storagePath).normalize();
    }
}
