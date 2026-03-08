package com.village.portal.dto.response;

import com.village.portal.enums.DocumentType;
import java.time.LocalDateTime;

public class DocumentResponse {

    private Long id;
    private Long projectId;
    private String projectNameEn;
    private String projectNameHi;
    private Long fundId;
    private String fundSchemeNameEn;
    private Long expenditureId;
    private DocumentType documentType;
    private String titleEn;
    private String titleHi;
    private String originalFileName;
    private String fileType;
    private Integer fileSizeKb;
    private Boolean isPublic;
    private String downloadUrl;
    private String uploadedByUsername;
    private LocalDateTime uploadedAt;

    public DocumentResponse() {}

    public Long getId()                          { return id; }
    public void setId(Long v)                    { this.id = v; }

    public Long getProjectId()                   { return projectId; }
    public void setProjectId(Long v)             { this.projectId = v; }

    public String getProjectNameEn()             { return projectNameEn; }
    public void setProjectNameEn(String v)       { this.projectNameEn = v; }

    public String getProjectNameHi()             { return projectNameHi; }
    public void setProjectNameHi(String v)       { this.projectNameHi = v; }

    public Long getFundId()                      { return fundId; }
    public void setFundId(Long v)                { this.fundId = v; }

    public String getFundSchemeNameEn()          { return fundSchemeNameEn; }
    public void setFundSchemeNameEn(String v)    { this.fundSchemeNameEn = v; }

    public Long getExpenditureId()               { return expenditureId; }
    public void setExpenditureId(Long v)         { this.expenditureId = v; }

    public DocumentType getDocumentType()        { return documentType; }
    public void setDocumentType(DocumentType v)  { this.documentType = v; }

    public String getTitleEn()                   { return titleEn; }
    public void setTitleEn(String v)             { this.titleEn = v; }

    public String getTitleHi()                   { return titleHi; }
    public void setTitleHi(String v)             { this.titleHi = v; }

    public String getOriginalFileName()          { return originalFileName; }
    public void setOriginalFileName(String v)    { this.originalFileName = v; }

    public String getFileType()                  { return fileType; }
    public void setFileType(String v)            { this.fileType = v; }

    public Integer getFileSizeKb()               { return fileSizeKb; }
    public void setFileSizeKb(Integer v)         { this.fileSizeKb = v; }

    public Boolean getIsPublic()                 { return isPublic; }
    public void setIsPublic(Boolean v)           { this.isPublic = v; }

    public String getDownloadUrl()               { return downloadUrl; }
    public void setDownloadUrl(String v)         { this.downloadUrl = v; }

    public String getUploadedByUsername()        { return uploadedByUsername; }
    public void setUploadedByUsername(String v)  { this.uploadedByUsername = v; }

    public LocalDateTime getUploadedAt()         { return uploadedAt; }
    public void setUploadedAt(LocalDateTime v)   { this.uploadedAt = v; }
}
