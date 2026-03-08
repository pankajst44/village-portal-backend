package com.village.portal.controller;

import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.DocumentResponse;
import com.village.portal.entity.Document;
import com.village.portal.enums.DocumentType;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.DocumentRepository;
import com.village.portal.service.DocumentService;
import com.village.portal.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService     documentService;
    private final FileStorageService  fileStorageService;
    private final DocumentRepository  documentRepository;

    public DocumentController(DocumentService documentService,
                               FileStorageService fileStorageService,
                               DocumentRepository documentRepository) {
        this.documentService    = documentService;
        this.fileStorageService = fileStorageService;
        this.documentRepository = documentRepository;
    }

    // ─────────────────────────────────────────────────────────────
    //  UPLOAD — Officers and Admins only
    // ─────────────────────────────────────────────────────────────

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file")                          MultipartFile file,
            @RequestParam("documentType")                  DocumentType  documentType,
            @RequestParam(value = "titleEn",    required = false) String titleEn,
            @RequestParam(value = "titleHi",    required = false) String titleHi,
            @RequestParam(value = "projectId",  required = false) Long   projectId,
            @RequestParam(value = "fundId",     required = false) Long   fundId,
            @RequestParam(value = "expenditureId", required = false) Long expenditureId,
            @RequestParam(value = "isPublic",   defaultValue = "true") Boolean isPublic) {

        DocumentResponse response = documentService.uploadDocument(
                file, documentType, titleEn, titleHi,
                projectId, fundId, expenditureId, isPublic);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    // ─────────────────────────────────────────────────────────────
    //  PUBLIC READ — no login required
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/public/project/{projectId}")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getPublicDocsByProject(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(ApiResponse.success(
                documentService.getPublicDocumentsByProject(projectId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  AUTHENTICATED READ
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(id)));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocsByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) DocumentType type) {

        List<DocumentResponse> result = (type != null)
                ? documentService.getDocumentsByProjectAndType(projectId, type)
                : documentService.getDocumentsByProject(projectId);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/fund/{fundId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocsByFund(
            @PathVariable Long fundId) {

        return ResponseEntity.ok(ApiResponse.success(
                documentService.getDocumentsByFund(fundId)));
    }

    @GetMapping("/expenditure/{expenditureId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocsByExpenditure(
            @PathVariable Long expenditureId) {

        return ResponseEntity.ok(ApiResponse.success(
                documentService.getDocumentsByExpenditure(expenditureId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  DOWNLOAD — public documents open to all; private require auth
    // ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {

        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        try {
            Path filePath = Paths.get(fileStorageService.getFilePath(document.getFileName()));
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File", "name", document.getFileName());
            }

            String contentType = document.getFileType() != null
                    ? document.getFileType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + document.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File", "name", document.getFileName());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE — Admin only
    // ─────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
    }
}
