package com.village.portal.service.impl;

import com.village.portal.exception.FileStorageException;
import com.village.portal.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    };

    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MB

    @Value("${app.storage.upload-dir:./uploads}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.uploadPath);
            log.info("File storage initialised at: {}", this.uploadPath);
        } catch (IOException ex) {
            throw new FileStorageException(
                    "Could not create upload directory at: " + uploadDir, ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {

        // ── Validate: not empty ──
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot store an empty file");
        }

        // ── Validate: file size ──
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileStorageException(
                    "File size exceeds maximum allowed limit of 10 MB");
        }

        // ── Validate: MIME type ──
        String contentType = file.getContentType();
        if (contentType == null ||
                Arrays.stream(ALLOWED_CONTENT_TYPES).noneMatch(t -> t.equalsIgnoreCase(contentType))) {
            throw new FileStorageException(
                    "File type '" + contentType + "' is not allowed. " +
                    "Accepted types: JPEG, PNG, PDF");
        }

        // ── Sanitise original filename ──
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");

        if (originalName.contains("..")) {
            throw new FileStorageException(
                    "Filename contains invalid path sequence: " + originalName);
        }

        // ── Generate a UUID-based stored filename ──
        String extension  = getExtension(originalName);
        String storedName = UUID.randomUUID().toString() + extension;

        try {
            Path targetPath = this.uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file '{}' as '{}'", originalName, storedName);
            return storedName;

        } catch (IOException ex) {
            throw new FileStorageException(
                    "Failed to store file '" + originalName + "'", ex);
        }
    }

    @Override
    public void deleteFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Path filePath = this.uploadPath.resolve(storedFileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted stored file: {}", storedFileName);
        } catch (IOException ex) {
            log.warn("Could not delete file '{}': {}", storedFileName, ex.getMessage());
        }
    }

    @Override
    public String getFilePath(String storedFileName) {
        return this.uploadPath.resolve(storedFileName).normalize().toString();
    }

    // ── Helpers ──

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex).toLowerCase() : "";
    }
}
