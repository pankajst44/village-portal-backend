package com.village.portal.dto.request;

import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateProjectRequest {

    private Long fundId;

    @NotBlank(message = "Project code is required")
    @Size(max = 30, message = "Project code must not exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Project code must contain only uppercase letters, digits and hyphens")
    private String projectCode;

    @NotBlank(message = "Project name (English) is required")
    @Size(max = 300, message = "Project name must not exceed 300 characters")
    private String nameEn;

    @Size(max = 300)
    private String nameHi;

    private String descriptionEn;
    private String descriptionHi;

    @Size(max = 200)
    private String locationEn;

    @Size(max = 200)
    private String locationHi;

    @NotNull(message = "Project type is required")
    private ProjectType projectType;

    private ProjectStatus status = ProjectStatus.PLANNED;

    @NotNull(message = "Allocated budget is required")
    @DecimalMin(value = "0.0", message = "Budget cannot be negative")
    @Digits(integer = 13, fraction = 2, message = "Invalid budget format")
    private BigDecimal allocatedBudget;

    @Min(value = 0, message = "Progress must be 0 or more")
    @Max(value = 100, message = "Progress cannot exceed 100")
    private Integer progressPercent = 0;

    private LocalDate startDate;
    private LocalDate expectedEndDate;

    private Long assignedOfficerId;

    private Boolean isPublicVisible = true;

    public CreateProjectRequest() {}

    public Long getFundId()                      { return fundId; }
    public void setFundId(Long v)                { this.fundId = v; }

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

    public Integer getProgressPercent()          { return progressPercent; }
    public void setProgressPercent(Integer v)    { this.progressPercent = v; }

    public LocalDate getStartDate()              { return startDate; }
    public void setStartDate(LocalDate v)        { this.startDate = v; }

    public LocalDate getExpectedEndDate()        { return expectedEndDate; }
    public void setExpectedEndDate(LocalDate v)  { this.expectedEndDate = v; }

    public Long getAssignedOfficerId()           { return assignedOfficerId; }
    public void setAssignedOfficerId(Long v)     { this.assignedOfficerId = v; }

    public Boolean getIsPublicVisible()          { return isPublicVisible; }
    public void setIsPublicVisible(Boolean v)    { this.isPublicVisible = v; }
}
