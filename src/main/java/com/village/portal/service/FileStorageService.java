package com.village.portal.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /** Saves file to root upload dir. Returns stored UUID filename. */
    String storeFile(MultipartFile file);

    /**
     * Saves file under a subdirectory within the upload dir.
     * e.g. subFolder = "complaints/1/2024/03/submission"
     * Returns relative path from upload root: "complaints/1/2024/03/submission/uuid.jpg"
     */
    String storeFile(MultipartFile file, String subFolder);

    /** Deletes a file by its relative path from upload root. */
    void deleteFile(String storedFileName);

    /** Returns absolute path string for a relative stored path. */
    String getFilePath(String storedFileName);
}
