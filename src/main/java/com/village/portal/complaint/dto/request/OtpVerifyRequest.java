package com.village.portal.complaint.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class OtpVerifyRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;

    public String getPhone()        { return phone; }
    public void setPhone(String v)  { this.phone = v; }
    public String getOtp()          { return otp; }
    public void setOtp(String v)    { this.otp = v; }
}
