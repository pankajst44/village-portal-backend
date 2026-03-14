package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.EvidenceType;
import com.village.portal.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_evidence")
public class ComplaintEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 30)
    private EvidenceType evidenceType;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "file_type", nullable = false, length = 50)
    private String fileType;

    @Column(name = "file_size_kb")
    private Integer fileSizeKb;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private User uploadedBy;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ComplaintEvidence() {}

    public Long getId()                         { return id; }
    public Complaint getComplaint()             { return complaint; }
    public void setComplaint(Complaint v)       { this.complaint = v; }
    public EvidenceType getEvidenceType()       { return evidenceType; }
    public void setEvidenceType(EvidenceType v) { this.evidenceType = v; }
    public String getFileName()                 { return fileName; }
    public void setFileName(String v)           { this.fileName = v; }
    public String getOriginalFileName()         { return originalFileName; }
    public void setOriginalFileName(String v)   { this.originalFileName = v; }
    public String getFileType()                 { return fileType; }
    public void setFileType(String v)           { this.fileType = v; }
    public Integer getFileSizeKb()              { return fileSizeKb; }
    public void setFileSizeKb(Integer v)        { this.fileSizeKb = v; }
    public String getFilePath()                 { return filePath; }
    public void setFilePath(String v)           { this.filePath = v; }
    public User getUploadedBy()                 { return uploadedBy; }
    public void setUploadedBy(User v)           { this.uploadedBy = v; }
    public Boolean getIsPublic()                { return isPublic; }
    public void setIsPublic(Boolean v)          { this.isPublic = v; }
    public Boolean getIsDeleted()               { return isDeleted; }
    public void setIsDeleted(Boolean v)         { this.isDeleted = v; }
    public LocalDateTime getDeletedAt()         { return deletedAt; }
    public void setDeletedAt(LocalDateTime v)   { this.deletedAt = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
}
