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
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024L;

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
            throw new FileStorageException("Could not create upload directory at: " + uploadDir, ex);
        }
    }

    // ── Original flat store (backwards compatible) ────────────
    @Override
    public String storeFile(MultipartFile file) {
        return storeFile(file, null);
    }

    // ── Store with subfolder (used by CMS evidence) ───────────
    @Override
    public String storeFile(MultipartFile file, String subFolder) {
        validate(file);

        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");

        if (originalName.contains("..")) {
            throw new FileStorageException("Filename contains invalid path sequence: " + originalName);
        }

        String extension  = getExtension(originalName);
        String storedName = UUID.randomUUID() + extension;

        try {
            Path targetDir;
            if (subFolder != null && !subFolder.isBlank()) {
                // Sanitise subFolder — strip leading slash, prevent path traversal
                String cleanSub = subFolder.replace("..", "").replaceAll("^/+", "");
                targetDir = this.uploadPath.resolve(cleanSub).normalize();
                // Safety: ensure resolved dir is still inside uploadPath
                if (!targetDir.startsWith(this.uploadPath)) {
                    throw new FileStorageException("Invalid subfolder path: " + subFolder);
                }
                Files.createDirectories(targetDir);
            } else {
                targetDir = this.uploadPath;
            }

            Path targetPath = targetDir.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path from upload root
            String relativePath = subFolder != null && !subFolder.isBlank()
                    ? subFolder.replaceAll("^/+", "") + "/" + storedName
                    : storedName;

            log.info("Stored file '{}' at '{}'", originalName, relativePath);
            return relativePath;

        } catch (IOException ex) {
            throw new FileStorageException("Failed to store file '" + originalName + "'", ex);
        }
    }

    @Override
    public void deleteFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) return;
        try {
            Path filePath = this.uploadPath.resolve(storedFileName).normalize();
            if (!filePath.startsWith(this.uploadPath)) {
                log.warn("Attempted path traversal on delete: {}", storedFileName);
                return;
            }
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", storedFileName);
        } catch (IOException ex) {
            log.warn("Could not delete file '{}': {}", storedFileName, ex.getMessage());
        }
    }

    @Override
    public String getFilePath(String storedFileName) {
        return this.uploadPath.resolve(storedFileName).normalize().toString();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("Cannot store an empty file");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileStorageException("File exceeds maximum allowed size of 10 MB");
        }
        String ct = file.getContentType();
        if (ct == null || Arrays.stream(ALLOWED_CONTENT_TYPES).noneMatch(t -> t.equalsIgnoreCase(ct))) {
            throw new FileStorageException(
                    "File type '" + ct + "' is not allowed. Accepted: JPEG, PNG, PDF");
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot).toLowerCase() : "";
    }
}
