package com.village.portal.complaint.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PostUpdateRequest {

    @NotBlank(message = "Note is required")
    @Size(min = 5, max = 2000)
    private String note;

    private Boolean isPublicNote = true;

    public String getNote()                 { return note; }
    public void setNote(String v)           { this.note = v; }
    public Boolean getIsPublicNote()        { return isPublicNote; }
    public void setIsPublicNote(Boolean v)  { this.isPublicNote = v; }
}
