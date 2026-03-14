package com.village.portal.complaint.dto.request;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class AssignComplaintRequest {

    @NotNull(message = "Officer ID is required")
    private Long officerId;

    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    public Long getOfficerId()          { return officerId; }
    public void setOfficerId(Long v)    { this.officerId = v; }
    public LocalDate getDueDate()       { return dueDate; }
    public void setDueDate(LocalDate v) { this.dueDate = v; }
}
