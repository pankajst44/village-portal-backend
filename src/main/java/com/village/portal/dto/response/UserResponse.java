package com.village.portal.dto.response;

import com.village.portal.enums.Role;
import java.time.LocalDateTime;

public class UserResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;

    public UserResponse() {}

    public Long getId()                     { return id; }
    public void setId(Long v)               { this.id = v; }

    public String getUsername()             { return username; }
    public void setUsername(String v)       { this.username = v; }

    public String getFullName()             { return fullName; }
    public void setFullName(String v)       { this.fullName = v; }

    public String getEmail()                { return email; }
    public void setEmail(String v)          { this.email = v; }

    public String getPhone()                { return phone; }
    public void setPhone(String v)          { this.phone = v; }

    public Role getRole()                   { return role; }
    public void setRole(Role v)             { this.role = v; }

    public Boolean getIsActive()            { return isActive; }
    public void setIsActive(Boolean v)      { this.isActive = v; }

    public LocalDateTime getLastLogin()     { return lastLogin; }
    public void setLastLogin(LocalDateTime v) { this.lastLogin = v; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
