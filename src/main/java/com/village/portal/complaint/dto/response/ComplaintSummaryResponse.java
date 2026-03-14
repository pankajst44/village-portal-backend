package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Lightweight DTO for list views — no evidence, no full timeline. */
public class ComplaintSummaryResponse {
    private Long id;
    private String complaintNumber;
    private String titleEn;
    private String titleHi;
    private String categoryNameEn;
    private String categoryNameHi;
    private Integer wardNumber;
    private String locationText;
    private ComplaintStatus status;
    private ComplaintPriority priority;
    private Integer escalationLevel;
    private Integer supportCount;
    private String submitterDisplayName;   // "Anonymous Resident" or full name
    private String assignedOfficerName;
    private LocalDate dueDate;
    private Boolean isOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId()                             { return id; }
    public void setId(Long v)                       { this.id = v; }
    public String getComplaintNumber()              { return complaintNumber; }
    public void setComplaintNumber(String v)        { this.complaintNumber = v; }
    public String getTitleEn()                      { return titleEn; }
    public void setTitleEn(String v)                { this.titleEn = v; }
    public String getTitleHi()                      { return titleHi; }
    public void setTitleHi(String v)                { this.titleHi = v; }
    public String getCategoryNameEn()               { return categoryNameEn; }
    public void setCategoryNameEn(String v)         { this.categoryNameEn = v; }
    public String getCategoryNameHi()               { return categoryNameHi; }
    public void setCategoryNameHi(String v)         { this.categoryNameHi = v; }
    public Integer getWardNumber()                  { return wardNumber; }
    public void setWardNumber(Integer v)            { this.wardNumber = v; }
    public String getLocationText()                 { return locationText; }
    public void setLocationText(String v)           { this.locationText = v; }
    public ComplaintStatus getStatus()              { return status; }
    public void setStatus(ComplaintStatus v)        { this.status = v; }
    public ComplaintPriority getPriority()          { return priority; }
    public void setPriority(ComplaintPriority v)    { this.priority = v; }
    public Integer getEscalationLevel()             { return escalationLevel; }
    public void setEscalationLevel(Integer v)       { this.escalationLevel = v; }
    public Integer getSupportCount()                { return supportCount; }
    public void setSupportCount(Integer v)          { this.supportCount = v; }
    public String getSubmitterDisplayName()         { return submitterDisplayName; }
    public void setSubmitterDisplayName(String v)   { this.submitterDisplayName = v; }
    public String getAssignedOfficerName()          { return assignedOfficerName; }
    public void setAssignedOfficerName(String v)    { this.assignedOfficerName = v; }
    public LocalDate getDueDate()                   { return dueDate; }
    public void setDueDate(LocalDate v)             { this.dueDate = v; }
    public Boolean getIsOverdue()                   { return isOverdue; }
    public void setIsOverdue(Boolean v)             { this.isOverdue = v; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public void setCreatedAt(LocalDateTime v)       { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()             { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)       { this.updatedAt = v; }
}
