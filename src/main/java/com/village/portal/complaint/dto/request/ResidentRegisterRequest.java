package com.village.portal.complaint.dto.request;

import javax.validation.constraints.*;

public class ResidentRegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, digits, dots, hyphens and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit Indian mobile number")
    private String phone;

    @NotNull(message = "Ward number is required")
    @Min(value = 1) @Max(value = 20)
    private Integer wardNumber;

    public String getFullName()             { return fullName; }
    public void setFullName(String v)       { this.fullName = v; }
    public String getUsername()             { return username; }
    public void setUsername(String v)       { this.username = v; }
    public String getPassword()             { return password; }
    public void setPassword(String v)       { this.password = v; }
    public String getPhone()                { return phone; }
    public void setPhone(String v)          { this.phone = v; }
    public Integer getWardNumber()          { return wardNumber; }
    public void setWardNumber(Integer v)    { this.wardNumber = v; }
}
