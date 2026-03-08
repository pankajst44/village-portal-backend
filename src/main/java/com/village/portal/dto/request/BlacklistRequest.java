package com.village.portal.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class BlacklistRequest {

    @NotBlank(message = "Blacklist reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    public BlacklistRequest() {}

    public String getReason()        { return reason; }
    public void setReason(String v)  { this.reason = v; }
}
