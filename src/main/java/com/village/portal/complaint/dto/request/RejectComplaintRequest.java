package com.village.portal.complaint.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RejectComplaintRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(min = 10, max = 500)
    private String reason;

    public String getReason()       { return reason; }
    public void setReason(String v) { this.reason = v; }
}
