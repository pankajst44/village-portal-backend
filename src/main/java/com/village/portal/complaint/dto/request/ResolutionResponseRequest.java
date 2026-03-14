package com.village.portal.complaint.dto.request;

import javax.validation.constraints.Size;

public class ResolutionResponseRequest {

    // Required only when rejecting — service validates
    @Size(min = 10, max = 500, message = "Rejection reason must be at least 10 characters")
    private String rejectionReason;

    public String getRejectionReason()      { return rejectionReason; }
    public void setRejectionReason(String v){ this.rejectionReason = v; }
}
