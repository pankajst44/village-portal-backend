package com.village.portal.complaint.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ResolveComplaintRequest {

    @NotBlank(message = "Resolution note is required")
    @Size(min = 20, max = 3000, message = "Resolution note must be at least 20 characters")
    private String resolutionNote;

    public String getResolutionNote()       { return resolutionNote; }
    public void setResolutionNote(String v) { this.resolutionNote = v; }
}
