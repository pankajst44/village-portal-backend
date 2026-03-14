package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Full detail DTO — includes timeline, evidence, resolution info. */
public class ComplaintDetailResponse {
    private Long id;
    private String complaintNumber;
    private String titleEn;
    private String titleHi;
    private String descriptionEn;
    private String descriptionHi;
    private String categoryNameEn;
    private String categoryNameHi;
    private Integer wardNumber;
    private String locationText;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private ComplaintStatus status;
    private ComplaintPriority priority;
    private Integer escalationLevel;
    private Integer supportCount;
    private Boolean hasVoted;           // populated for authenticated residents
    private String submitterDisplayName;
    private String assignedOfficerName;
    private LocalDate dueDate;
    private Boolean isOverdue;
    private String resolutionNote;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    private LocalDateTime citizenResponseDeadline;
    private String rejectionReason;
    private Boolean isDuplicateFlagged;
    private String duplicateOfNumber;
    private List<TimelineEntryResponse> timeline;
    private List<EvidenceResponse> evidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId()                                 { return id; }
    public void setId(Long v)                           { this.id = v; }
    public String getComplaintNumber()                  { return complaintNumber; }
    public void setComplaintNumber(String v)            { this.complaintNumber = v; }
    public String getTitleEn()                          { return titleEn; }
    public void setTitleEn(String v)                    { this.titleEn = v; }
    public String getTitleHi()                          { return titleHi; }
    public void setTitleHi(String v)                    { this.titleHi = v; }
    public String getDescriptionEn()                    { return descriptionEn; }
    public void setDescriptionEn(String v)              { this.descriptionEn = v; }
    public String getDescriptionHi()                    { return descriptionHi; }
    public void setDescriptionHi(String v)              { this.descriptionHi = v; }
    public String getCategoryNameEn()                   { return categoryNameEn; }
    public void setCategoryNameEn(String v)             { this.categoryNameEn = v; }
    public String getCategoryNameHi()                   { return categoryNameHi; }
    public void setCategoryNameHi(String v)             { this.categoryNameHi = v; }
    public Integer getWardNumber()                      { return wardNumber; }
    public void setWardNumber(Integer v)                { this.wardNumber = v; }
    public String getLocationText()                     { return locationText; }
    public void setLocationText(String v)               { this.locationText = v; }
    public BigDecimal getLatitude()                     { return latitude; }
    public void setLatitude(BigDecimal v)               { this.latitude = v; }
    public BigDecimal getLongitude()                    { return longitude; }
    public void setLongitude(BigDecimal v)              { this.longitude = v; }
    public ComplaintStatus getStatus()                  { return status; }
    public void setStatus(ComplaintStatus v)            { this.status = v; }
    public ComplaintPriority getPriority()              { return priority; }
    public void setPriority(ComplaintPriority v)        { this.priority = v; }
    public Integer getEscalationLevel()                 { return escalationLevel; }
    public void setEscalationLevel(Integer v)           { this.escalationLevel = v; }
    public Integer getSupportCount()                    { return supportCount; }
    public void setSupportCount(Integer v)              { this.supportCount = v; }
    public Boolean getHasVoted()                        { return hasVoted; }
    public void setHasVoted(Boolean v)                  { this.hasVoted = v; }
    public String getSubmitterDisplayName()             { return submitterDisplayName; }
    public void setSubmitterDisplayName(String v)       { this.submitterDisplayName = v; }
    public String getAssignedOfficerName()              { return assignedOfficerName; }
    public void setAssignedOfficerName(String v)        { this.assignedOfficerName = v; }
    public LocalDate getDueDate()                       { return dueDate; }
    public void setDueDate(LocalDate v)                 { this.dueDate = v; }
    public Boolean getIsOverdue()                       { return isOverdue; }
    public void setIsOverdue(Boolean v)                 { this.isOverdue = v; }
    public String getResolutionNote()                   { return resolutionNote; }
    public void setResolutionNote(String v)             { this.resolutionNote = v; }
    public LocalDateTime getResolvedAt()                { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v)          { this.resolvedAt = v; }
    public LocalDateTime getClosedAt()                  { return closedAt; }
    public void setClosedAt(LocalDateTime v)            { this.closedAt = v; }
    public LocalDateTime getCitizenResponseDeadline()   { return citizenResponseDeadline; }
    public void setCitizenResponseDeadline(LocalDateTime v){ this.citizenResponseDeadline = v; }
    public String getRejectionReason()                  { return rejectionReason; }
    public void setRejectionReason(String v)            { this.rejectionReason = v; }
    public Boolean getIsDuplicateFlagged()              { return isDuplicateFlagged; }
    public void setIsDuplicateFlagged(Boolean v)        { this.isDuplicateFlagged = v; }
    public String getDuplicateOfNumber()                { return duplicateOfNumber; }
    public void setDuplicateOfNumber(String v)          { this.duplicateOfNumber = v; }
    public List<TimelineEntryResponse> getTimeline()    { return timeline; }
    public void setTimeline(List<TimelineEntryResponse> v){ this.timeline = v; }
    public List<EvidenceResponse> getEvidence()         { return evidence; }
    public void setEvidence(List<EvidenceResponse> v)   { this.evidence = v; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime v)           { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)           { this.updatedAt = v; }
}
