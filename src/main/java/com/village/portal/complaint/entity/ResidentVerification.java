package com.village.portal.complaint.entity;

import com.village.portal.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resident_verifications")
public class ResidentVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "otp_hash", length = 64)
    private String otpHash;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public ResidentVerification() {}

    public Long getId()                         { return id; }
    public User getUser()                       { return user; }
    public void setUser(User v)                 { this.user = v; }
    public String getPhoneNumber()              { return phoneNumber; }
    public void setPhoneNumber(String v)        { this.phoneNumber = v; }
    public String getOtpHash()                  { return otpHash; }
    public void setOtpHash(String v)            { this.otpHash = v; }
    public LocalDateTime getOtpExpiresAt()      { return otpExpiresAt; }
    public void setOtpExpiresAt(LocalDateTime v){ this.otpExpiresAt = v; }
    public Boolean getIsVerified()              { return isVerified; }
    public void setIsVerified(Boolean v)        { this.isVerified = v; }
    public LocalDateTime getVerifiedAt()        { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime v)  { this.verifiedAt = v; }
    public Integer getAttempts()                { return attempts; }
    public void setAttempts(Integer v)          { this.attempts = v; }
    public String getIpAddress()                { return ipAddress; }
    public void setIpAddress(String v)          { this.ipAddress = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
}
