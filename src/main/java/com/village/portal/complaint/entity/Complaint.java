package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import com.village.portal.entity.User;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_number", nullable = false, unique = true, length = 20)
    private String complaintNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_id", nullable = false)
    private Village village;

    @Column(name = "ward_number")
    private Integer wardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ComplaintCategory category;

    @Column(name = "title_en", nullable = false, length = 200)
    private String titleEn;

    @Column(name = "title_hi", length = 200)
    private String titleHi;

    @Column(name = "description_en", columnDefinition = "TEXT", nullable = false)
    private String descriptionEn;

    @Column(name = "description_hi", columnDefinition = "TEXT")
    private String descriptionHi;

    @Column(name = "location_text", nullable = false, length = 300)
    private String locationText;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ComplaintStatus status = ComplaintStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 10)
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitter_user_id", nullable = false)
    private User submitter;

    @Column(name = "submitter_phone_verified", nullable = false)
    private Boolean submitterPhoneVerified = false;

    @Column(name = "is_anonymous_display", nullable = false)
    private Boolean isAnonymousDisplay = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer_id")
    private User assignedOfficer;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "escalation_level", nullable = false)
    private Integer escalationLevel = 0;

    @Column(name = "last_escalated_at")
    private LocalDateTime lastEscalatedAt;

    @Column(name = "support_count", nullable = false)
    private Integer supportCount = 0;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = true;

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "citizen_response_deadline")
    private LocalDateTime citizenResponseDeadline;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "is_duplicate_flagged", nullable = false)
    private Boolean isDuplicateFlagged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duplicate_of_id")
    private Complaint duplicateOf;

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

    public Complaint() {}

    // ── Getters / Setters ─────────────────────────────────────
    public Long getId()                              { return id; }
    public String getComplaintNumber()               { return complaintNumber; }
    public void setComplaintNumber(String v)         { this.complaintNumber = v; }
    public Village getVillage()                      { return village; }
    public void setVillage(Village v)                { this.village = v; }
    public Integer getWardNumber()                   { return wardNumber; }
    public void setWardNumber(Integer v)             { this.wardNumber = v; }
    public ComplaintCategory getCategory()           { return category; }
    public void setCategory(ComplaintCategory v)     { this.category = v; }
    public String getTitleEn()                       { return titleEn; }
    public void setTitleEn(String v)                 { this.titleEn = v; }
    public String getTitleHi()                       { return titleHi; }
    public void setTitleHi(String v)                 { this.titleHi = v; }
    public String getDescriptionEn()                 { return descriptionEn; }
    public void setDescriptionEn(String v)           { this.descriptionEn = v; }
    public String getDescriptionHi()                 { return descriptionHi; }
    public void setDescriptionHi(String v)           { this.descriptionHi = v; }
    public String getLocationText()                  { return locationText; }
    public void setLocationText(String v)            { this.locationText = v; }
    public BigDecimal getLatitude()                  { return latitude; }
    public void setLatitude(BigDecimal v)            { this.latitude = v; }
    public BigDecimal getLongitude()                 { return longitude; }
    public void setLongitude(BigDecimal v)           { this.longitude = v; }
    public ComplaintStatus getStatus()               { return status; }
    public void setStatus(ComplaintStatus v)         { this.status = v; }
    public ComplaintPriority getPriority()           { return priority; }
    public void setPriority(ComplaintPriority v)     { this.priority = v; }
    public User getSubmitter()                       { return submitter; }
    public void setSubmitter(User v)                 { this.submitter = v; }
    public Boolean getSubmitterPhoneVerified()       { return submitterPhoneVerified; }
    public void setSubmitterPhoneVerified(Boolean v) { this.submitterPhoneVerified = v; }
    public Boolean getIsAnonymousDisplay()           { return isAnonymousDisplay; }
    public void setIsAnonymousDisplay(Boolean v)     { this.isAnonymousDisplay = v; }
    public User getAssignedOfficer()                 { return assignedOfficer; }
    public void setAssignedOfficer(User v)           { this.assignedOfficer = v; }
    public LocalDateTime getAssignedAt()             { return assignedAt; }
    public void setAssignedAt(LocalDateTime v)       { this.assignedAt = v; }
    public LocalDate getDueDate()                    { return dueDate; }
    public void setDueDate(LocalDate v)              { this.dueDate = v; }
    public Integer getEscalationLevel()              { return escalationLevel; }
    public void setEscalationLevel(Integer v)        { this.escalationLevel = v; }
    public LocalDateTime getLastEscalatedAt()        { return lastEscalatedAt; }
    public void setLastEscalatedAt(LocalDateTime v)  { this.lastEscalatedAt = v; }
    public Integer getSupportCount()                 { return supportCount; }
    public void setSupportCount(Integer v)           { this.supportCount = v; }
    public Boolean getIsPublic()                     { return isPublic; }
    public void setIsPublic(Boolean v)               { this.isPublic = v; }
    public String getResolutionNote()                { return resolutionNote; }
    public void setResolutionNote(String v)          { this.resolutionNote = v; }
    public LocalDateTime getResolvedAt()             { return resolvedAt; }
    public void setResolvedAt(LocalDateTime v)       { this.resolvedAt = v; }
    public LocalDateTime getClosedAt()               { return closedAt; }
    public void setClosedAt(LocalDateTime v)         { this.closedAt = v; }
    public LocalDateTime getCitizenResponseDeadline(){ return citizenResponseDeadline; }
    public void setCitizenResponseDeadline(LocalDateTime v){ this.citizenResponseDeadline = v; }
    public String getRejectionReason()               { return rejectionReason; }
    public void setRejectionReason(String v)         { this.rejectionReason = v; }
    public Boolean getIsDuplicateFlagged()           { return isDuplicateFlagged; }
    public void setIsDuplicateFlagged(Boolean v)     { this.isDuplicateFlagged = v; }
    public Complaint getDuplicateOf()                { return duplicateOf; }
    public void setDuplicateOf(Complaint v)          { this.duplicateOf = v; }
    public LocalDateTime getCreatedAt()              { return createdAt; }
    public LocalDateTime getUpdatedAt()              { return updatedAt; }
}
