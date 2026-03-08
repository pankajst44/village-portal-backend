package com.village.portal.service;

import com.village.portal.dto.response.DocumentResponse;
import com.village.portal.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(
            MultipartFile file,
            DocumentType documentType,
            String titleEn,
            String titleHi,
            Long projectId,
            Long fundId,
            Long expenditureId,
            Boolean isPublic);

    DocumentResponse getDocumentById(Long id);

    List<DocumentResponse> getDocumentsByProject(Long projectId);

    List<DocumentResponse> getPublicDocumentsByProject(Long projectId);

    List<DocumentResponse> getDocumentsByProjectAndType(Long projectId, DocumentType type);

    List<DocumentResponse> getDocumentsByFund(Long fundId);

    List<DocumentResponse> getDocumentsByExpenditure(Long expenditureId);

    void deleteDocument(Long id);
}
