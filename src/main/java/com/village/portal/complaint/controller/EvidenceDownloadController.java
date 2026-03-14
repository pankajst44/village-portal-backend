package com.village.portal.complaint.controller;

import com.village.portal.complaint.entity.ComplaintEvidence;
import com.village.portal.complaint.repository.ComplaintEvidenceRepository;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/complaints")
public class EvidenceDownloadController {

    private final ComplaintEvidenceRepository evidenceRepository;
    private final FileStorageService          fileStorageService;

    public EvidenceDownloadController(ComplaintEvidenceRepository evidenceRepository,
                                       FileStorageService fileStorageService) {
        this.evidenceRepository = evidenceRepository;
        this.fileStorageService  = fileStorageService;
    }

    @GetMapping("/{complaintId}/evidence/{evidenceId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long complaintId,
            @PathVariable Long evidenceId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found","",""));

        if (!evidence.getComplaint().getId().equals(complaintId)) {
            throw new ResourceNotFoundException("Evidence not found for this complaint","","");
        }

        if (Boolean.TRUE.equals(evidence.getIsDeleted())) {
            throw new BusinessException("FILE_DELETED", "This file has been removed.");
        }

        // Non-public evidence requires authentication + ownership/staff role
        if (!evidence.getIsPublic()) {
            if (currentUser == null) {
                throw new BusinessException("ACCESS_DENIED", "Authentication required.");
            }
            boolean isOwner = evidence.getComplaint().getSubmitter().getId().equals(currentUser.getId());
            boolean isStaff = currentUser.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_OFFICER")
                            || a.getAuthority().equals("ROLE_AUDITOR"));
            if (!isOwner && !isStaff) {
                throw new BusinessException("ACCESS_DENIED", "You do not have access to this file.");
            }
        }

        String absolutePath = fileStorageService.getFilePath(evidence.getFilePath());
        File file = new File(absolutePath);
        if (!file.exists()) {
            throw new ResourceNotFoundException("File not found on storage","","");
        }

        Resource resource = new FileSystemResource(file);
        String contentType = evidence.getFileType() != null
                ? evidence.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + evidence.getOriginalFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(file.length())
                .body(resource);
    }
}
