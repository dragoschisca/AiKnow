package com.aiknow.document;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.UUID;

public interface FileStorageService {
    String store(UUID organizationId, UUID workspaceId, String filename, InputStream inputStream);
    InputStream load(String storagePath);
    void delete(String storagePath);
    Path getStorageLocation(String storagePath);
}
