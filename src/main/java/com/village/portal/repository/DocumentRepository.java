package com.village.portal.repository;

import com.village.portal.entity.Document;
import com.village.portal.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByProjectId(Long projectId);

    List<Document> findByProjectIdAndIsPublicTrue(Long projectId);

    List<Document> findByProjectIdAndDocumentType(Long projectId, DocumentType documentType);

    List<Document> findByFundId(Long fundId);

    List<Document> findByExpenditureId(Long expenditureId);

    List<Document> findByUploadedById(Long uploadedById);
}
