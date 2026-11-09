package com.zerohundreds.hunt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ImageStorageService {

    private final ImageStorageAdapter storageAdapter;

    @Value("${app.storage.provider:LOCAL_DISK}")
    private String storageProviderType;

    public ImageStorageService(ImageStorageAdapter storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    public static class StoredFile {
        public String storageKey;
        public String storagePath;
        public String servingUrl;
        public String provider;
    }

    public StoredFile savePermanentImage(Long userId, Long visualizationId, String prefix, byte[] data, String extension) throws IOException {
        String cleanExt = (extension != null && !extension.isBlank()) ? extension.toLowerCase().replace(".", "") : "jpg";
        String contentType = cleanExt.equals("png") ? "image/png" : (cleanExt.equals("svg") ? "image/svg+xml" : "image/jpeg");
        String fileName = prefix + "." + cleanExt;

        ImageStorageAdapter.StoredObject obj = storageAdapter.storeImage(userId, visualizationId, fileName, data, contentType);

        StoredFile sf = new StoredFile();
        sf.storageKey = obj.storageKey;
        sf.storagePath = obj.storageKey;
        sf.servingUrl = obj.publicUrl;
        sf.provider = obj.storageProvider;
        return sf;
    }

    public StoredFile saveSvgIllustration(Long userId, Long visualizationId, String prefix, String svgContent) throws IOException {
        byte[] bytes = svgContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return savePermanentImage(userId, visualizationId, prefix, bytes, "svg");
    }

    public byte[] readImageBytes(String storageKey) throws IOException {
        return storageAdapter.retrieveImage(storageKey);
    }
}
