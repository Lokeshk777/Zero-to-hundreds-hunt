package com.zerohundreds.hunt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Primary
public class LocalDiskStorageAdapter implements ImageStorageAdapter {

    @Value("${app.storage.base-path:storage/story-visualizations}")
    private String storageBasePath;

    @Value("${app.server.base-url:}")
    private String serverBaseUrl;

    @Override
    public StoredObject storeImage(Long userId, Long visualizationId, String fileName, byte[] data, String contentType) throws IOException {
        String userDir = (userId != null ? userId.toString() : "public");
        String vizDir = (visualizationId != null ? visualizationId.toString() : "shared");

        Path targetDir = Paths.get(storageBasePath, userDir, vizDir);
        Files.createDirectories(targetDir);

        String uniqueName = UUID.randomUUID().toString().substring(0, 8) + "_" + fileName;
        Path targetFile = targetDir.resolve(uniqueName);

        try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
            fos.write(data);
        }

        StoredObject obj = new StoredObject();
        obj.storageKey = userDir + "/" + vizDir + "/" + uniqueName;
        obj.storageProvider = "LOCAL_DISK";
        
        // Relative serving path (or prefixed with serverBaseUrl if present)
        String relativeUrl = "/storage/story-visualizations/" + obj.storageKey;
        obj.publicUrl = (serverBaseUrl != null && !serverBaseUrl.isBlank()) 
                ? (serverBaseUrl.endsWith("/") ? serverBaseUrl.substring(0, serverBaseUrl.length() - 1) : serverBaseUrl) + relativeUrl
                : relativeUrl;

        return obj;
    }

    @Override
    public byte[] retrieveImage(String storageKey) throws IOException {
        Path p = Paths.get(storageBasePath, storageKey);
        if (Files.exists(p)) {
            return Files.readAllBytes(p);
        }
        return null;
    }

    @Override
    public void deleteImage(String storageKey) throws IOException {
        Path p = Paths.get(storageBasePath, storageKey);
        Files.deleteIfExists(p);
    }
}
