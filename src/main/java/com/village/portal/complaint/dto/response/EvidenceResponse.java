package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.EvidenceType;
import java.time.LocalDateTime;

public class EvidenceResponse {
    private Long id;
    private EvidenceType evidenceType;
    private String originalFileName;
    private String fileType;
    private Integer fileSizeKb;
    private String downloadUrl;   // /api/complaints/{id}/evidence/{evidenceId}/download
    private Boolean isPublic;
    private String uploadedByName;
    private LocalDateTime createdAt;

    public Long getId()                         { return id; }
    public void setId(Long v)                   { this.id = v; }
    public EvidenceType getEvidenceType()       { return evidenceType; }
    public void setEvidenceType(EvidenceType v) { this.evidenceType = v; }
    public String getOriginalFileName()         { return originalFileName; }
    public void setOriginalFileName(String v)   { this.originalFileName = v; }
    public String getFileType()                 { return fileType; }
    public void setFileType(String v)           { this.fileType = v; }
    public Integer getFileSizeKb()              { return fileSizeKb; }
    public void setFileSizeKb(Integer v)        { this.fileSizeKb = v; }
    public String getDownloadUrl()              { return downloadUrl; }
    public void setDownloadUrl(String v)        { this.downloadUrl = v; }
    public Boolean getIsPublic()                { return isPublic; }
    public void setIsPublic(Boolean v)          { this.isPublic = v; }
    public String getUploadedByName()           { return uploadedByName; }
    public void setUploadedByName(String v)     { this.uploadedByName = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
}
