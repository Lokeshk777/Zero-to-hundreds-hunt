package com.zerohundreds.hunt.service;

import java.io.IOException;

public interface ImageStorageAdapter {

    class StoredObject {
        public String storageKey;    // Relative path or S3/Cloud object key
        public String publicUrl;     // URL to fetch or render
        public String storageProvider; // "LOCAL_DISK" or "CLOUD_OBJECT_STORAGE"
    }

    StoredObject storeImage(Long userId, Long visualizationId, String fileName, byte[] data, String contentType) throws IOException;

    byte[] retrieveImage(String storageKey) throws IOException;

    void deleteImage(String storageKey) throws IOException;
}
