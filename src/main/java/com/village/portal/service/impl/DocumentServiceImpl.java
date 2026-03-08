package com.village.portal.service.impl;

import com.village.portal.aspect.Auditable;
import com.village.portal.dto.response.DocumentResponse;
import com.village.portal.entity.Document;
import com.village.portal.entity.Expenditure;
import com.village.portal.entity.Fund;
import com.village.portal.entity.Project;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.DocumentType;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.DocumentRepository;
import com.village.portal.repository.ExpenditureRepository;
import com.village.portal.repository.FundRepository;
import com.village.portal.repository.ProjectRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.DocumentService;
import com.village.portal.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository   documentRepository;
    private final ProjectRepository    projectRepository;
    private final FundRepository       fundRepository;
    private final ExpenditureRepository expenditureRepository;
    private final UserRepository       userRepository;
    private final FileStorageService   fileStorageService;

    public DocumentServiceImpl(DocumentRepository documentRepository,
                                ProjectRepository projectRepository,
                                FundRepository fundRepository,
                                ExpenditureRepository expenditureRepository,
                                UserRepository userRepository,
                                FileStorageService fileStorageService) {
        this.documentRepository   = documentRepository;
        this.projectRepository    = projectRepository;
        this.fundRepository       = fundRepository;
        this.expenditureRepository = expenditureRepository;
        this.userRepository       = userRepository;
        this.fileStorageService   = fileStorageService;
    }

    // ─────────────────────────────────────────────────────────────
    //  UPLOAD
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.FILE_UPLOAD, tableName = "documents",
               description = "Document uploaded")
    public DocumentResponse uploadDocument(MultipartFile file,
                                           DocumentType documentType,
                                           String titleEn,
                                           String titleHi,
                                           Long projectId,
                                           Long fundId,
                                           Long expenditureId,
                                           Boolean isPublic) {

        // ── Business rule: at least one parent reference required ──
        if (projectId == null && fundId == null && expenditureId == null) {
            throw new BusinessException("MISSING_PARENT",
                    "A document must be linked to a project, a fund, or an expenditure");
        }

        // ── Resolve optional parent entities ──
        Project project = null;
        if (projectId != null) {
            project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        }

        Fund fund = null;
        if (fundId != null) {
            fund = fundRepository.findById(fundId)
                    .orElseThrow(() -> new ResourceNotFoundException("Fund", "id", fundId));
        }

        Expenditure expenditure = null;
        if (expenditureId != null) {
            expenditure = expenditureRepository.findById(expenditureId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Expenditure", "id", expenditureId));
        }

        // ── Persist the file to storage ──
        String storedFileName = fileStorageService.storeFile(file);
        String filePath       = fileStorageService.getFilePath(storedFileName);

        // ── Build and save document metadata ──
        Document document = new Document();
        document.setProject(project);
        document.setFund(fund);
        document.setExpenditure(expenditure);
        document.setDocumentType(documentType != null ? documentType : DocumentType.OTHER);
        document.setTitleEn(titleEn);
        document.setTitleHi(titleHi);
        document.setFileName(storedFileName);
        document.setOriginalFileName(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : storedFileName);
        document.setFilePath(filePath);
        document.setFileType(file.getContentType());
        document.setFileSizeKb((int) (file.getSize() / 1024));
        document.setIsPublic(isPublic != null ? isPublic : true);
        document.setUploadedBy(getCurrentUser());

        Document saved = documentRepository.save(document);
        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────────
    //  READ
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return toResponse(document);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByProject(Long projectId) {
        validateProjectExists(projectId);
        return documentRepository.findByProjectId(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getPublicDocumentsByProject(Long projectId) {
        validateProjectExists(projectId);
        return documentRepository.findByProjectIdAndIsPublicTrue(projectId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByProjectAndType(Long projectId, DocumentType type) {
        validateProjectExists(projectId);
        return documentRepository.findByProjectIdAndDocumentType(projectId, type)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByFund(Long fundId) {
        if (!fundRepository.existsById(fundId)) {
            throw new ResourceNotFoundException("Fund", "id", fundId);
        }
        return documentRepository.findByFundId(fundId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByExpenditure(Long expenditureId) {
        if (!expenditureRepository.existsById(expenditureId)) {
            throw new ResourceNotFoundException("Expenditure", "id", expenditureId);
        }
        return documentRepository.findByExpenditureId(expenditureId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.FILE_DELETE, tableName = "documents",
               description = "Document deleted")
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        // Remove physical file from storage
        fileStorageService.deleteFile(document.getFileName());

        // Remove metadata record from DB
        documentRepository.delete(document);
    }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    private void validateProjectExists(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl details = (UserDetailsImpl) auth.getPrincipal();
            return userRepository.findById(details.getId()).orElse(null);
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────────
    //  MAPPER  Entity → Response DTO
    // ─────────────────────────────────────────────────────────────

    private DocumentResponse toResponse(Document d) {
        DocumentResponse r = new DocumentResponse();
        r.setId(d.getId());
        r.setDocumentType(d.getDocumentType());
        r.setTitleEn(d.getTitleEn());
        r.setTitleHi(d.getTitleHi());
        r.setOriginalFileName(d.getOriginalFileName());
        r.setFileType(d.getFileType());
        r.setFileSizeKb(d.getFileSizeKb());
        r.setIsPublic(d.getIsPublic());
        r.setUploadedAt(d.getUploadedAt());

        // Build a public download URL from stored file name
        try {
            String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/documents/")
                    .path(String.valueOf(d.getId()))
                    .path("/download")
                    .toUriString();
            r.setDownloadUrl(downloadUrl);
        } catch (Exception e) {
            // If called outside a request context (e.g. tests), skip URL building
            r.setDownloadUrl("/documents/" + d.getId() + "/download");
        }

        if (d.getProject() != null) {
            r.setProjectId(d.getProject().getId());
            r.setProjectNameEn(d.getProject().getNameEn());
            r.setProjectNameHi(d.getProject().getNameHi());
        }

        if (d.getFund() != null) {
            r.setFundId(d.getFund().getId());
            r.setFundSchemeNameEn(d.getFund().getSchemeNameEn());
        }

        if (d.getExpenditure() != null) {
            r.setExpenditureId(d.getExpenditure().getId());
        }

        if (d.getUploadedBy() != null) {
            r.setUploadedByUsername(d.getUploadedBy().getUsername());
        }

        return r;
    }
}
