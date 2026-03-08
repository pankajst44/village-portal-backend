package com.village.portal.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * Saves the file to the configured upload directory.
     * Returns the stored file name (UUID-based, not the original name).
     */
    String storeFile(MultipartFile file);

    /**
     * Deletes a stored file by its stored file name.
     * Silently ignores missing files.
     */
    void deleteFile(String storedFileName);

    /**
     * Returns the absolute path string for a stored file name.
     */
    String getFilePath(String storedFileName);
}
