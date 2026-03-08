package com.village.portal.dto.request;

import com.village.portal.enums.ProjectStatus;

import javax.validation.constraints.*;

public class UpdateProjectProgressRequest {

    @NotNull(message = "Progress percent is required")
    @Min(value = 0, message = "Progress must be 0 or more")
    @Max(value = 100, message = "Progress cannot exceed 100")
    private Integer progressPercent;

    private ProjectStatus status;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;

    public UpdateProjectProgressRequest() {}

    public Integer getProgressPercent()       { return progressPercent; }
    public void setProgressPercent(Integer v) { this.progressPercent = v; }

    public ProjectStatus getStatus()          { return status; }
    public void setStatus(ProjectStatus v)    { this.status = v; }

    public String getRemarks()                { return remarks; }
    public void setRemarks(String v)          { this.remarks = v; }
}
