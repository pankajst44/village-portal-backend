package com.village.portal.dto.request;

import com.village.portal.enums.Role;

import javax.validation.constraints.*;

public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
             message = "Username can only contain letters, digits, dots, hyphens and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100)
    private String email;

    @Pattern(regexp = "^[6-9]\\d{9}$",
             message = "Please provide a valid 10-digit Indian mobile number")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;

    public CreateUserRequest() {}

    public String getUsername()  { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword()  { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName()  { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail()     { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone()     { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Role getRole()        { return role; }
    public void setRole(Role role) { this.role = role; }
}
