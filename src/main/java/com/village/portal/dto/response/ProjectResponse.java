package com.village.portal.dto.response;

import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectResponse {

    private Long id;
    private Long fundId;
    private String fundSchemeNameEn;
    private String fundSchemeNameHi;
    private String projectCode;
    private String nameEn;
    private String nameHi;
    private String descriptionEn;
    private String descriptionHi;
    private String locationEn;
    private String locationHi;
    private ProjectType projectType;
    private ProjectStatus status;
    private BigDecimal allocatedBudget;
    private BigDecimal totalSpent;
    private BigDecimal remainingBudget;
    private Integer progressPercent;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private LocalDate actualEndDate;
    private Long assignedOfficerId;
    private String assignedOfficerName;
    private Boolean isPublicVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectResponse() {}

    public Long getId()                          { return id; }
    public void setId(Long v)                    { this.id = v; }

    public Long getFundId()                      { return fundId; }
    public void setFundId(Long v)                { this.fundId = v; }

    public String getFundSchemeNameEn()          { return fundSchemeNameEn; }
    public void setFundSchemeNameEn(String v)    { this.fundSchemeNameEn = v; }

    public String getFundSchemeNameHi()          { return fundSchemeNameHi; }
    public void setFundSchemeNameHi(String v)    { this.fundSchemeNameHi = v; }

    public String getProjectCode()               { return projectCode; }
    public void setProjectCode(String v)         { this.projectCode = v; }

    public String getNameEn()                    { return nameEn; }
    public void setNameEn(String v)              { this.nameEn = v; }

    public String getNameHi()                    { return nameHi; }
    public void setNameHi(String v)              { this.nameHi = v; }

    public String getDescriptionEn()             { return descriptionEn; }
    public void setDescriptionEn(String v)       { this.descriptionEn = v; }

    public String getDescriptionHi()             { return descriptionHi; }
    public void setDescriptionHi(String v)       { this.descriptionHi = v; }

    public String getLocationEn()                { return locationEn; }
    public void setLocationEn(String v)          { this.locationEn = v; }

    public String getLocationHi()                { return locationHi; }
    public void setLocationHi(String v)          { this.locationHi = v; }

    public ProjectType getProjectType()          { return projectType; }
    public void setProjectType(ProjectType v)    { this.projectType = v; }

    public ProjectStatus getStatus()             { return status; }
    public void setStatus(ProjectStatus v)       { this.status = v; }

    public BigDecimal getAllocatedBudget()        { return allocatedBudget; }
    public void setAllocatedBudget(BigDecimal v) { this.allocatedBudget = v; }

    public BigDecimal getTotalSpent()            { return totalSpent; }
    public void setTotalSpent(BigDecimal v)      { this.totalSpent = v; }

    public BigDecimal getRemainingBudget()       { return remainingBudget; }
    public void setRemainingBudget(BigDecimal v) { this.remainingBudget = v; }

    public Integer getProgressPercent()          { return progressPercent; }
    public void setProgressPercent(Integer v)    { this.progressPercent = v; }

    public LocalDate getStartDate()              { return startDate; }
    public void setStartDate(LocalDate v)        { this.startDate = v; }

    public LocalDate getExpectedEndDate()        { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate v)  { this.expectedEndDate = v; }

    public LocalDate getActualEndDate()          { return actualEndDate; }
    public void setActualEndDate(LocalDate v)    { this.actualEndDate = v; }

    public Long getAssignedOfficerId()           { return assignedOfficerId; }
    public void setAssignedOfficerId(Long v)     { this.assignedOfficerId = v; }

    public String getAssignedOfficerName()       { return assignedOfficerName; }
    public void setAssignedOfficerName(String v) { this.assignedOfficerName = v; }

    public Boolean getIsPublicVisible()          { return isPublicVisible; }
    public void setIsPublicVisible(Boolean v)    { this.isPublicVisible = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()          { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)    { this.updatedAt = v; }
}
