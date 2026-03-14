package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.ComplaintStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_timeline")
public class ComplaintTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ComplaintStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private ComplaintStatus toStatus;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @Column(name = "actor_role", nullable = false, length = 20)
    private String actorRole;

    @Column(name = "actor_name", length = 100)
    private String actorName;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_public_note", nullable = false)
    private Boolean isPublicNote = true;

    // Immutable — no @PreUpdate, no updated_at
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ComplaintTimeline() {}

    public Long getId()                         { return id; }
    public Complaint getComplaint()             { return complaint; }
    public void setComplaint(Complaint v)       { this.complaint = v; }
    public ComplaintStatus getFromStatus()      { return fromStatus; }
    public void setFromStatus(ComplaintStatus v){ this.fromStatus = v; }
    public ComplaintStatus getToStatus()        { return toStatus; }
    public void setToStatus(ComplaintStatus v)  { this.toStatus = v; }
    public Long getActorUserId()                { return actorUserId; }
    public void setActorUserId(Long v)          { this.actorUserId = v; }
    public String getActorRole()                { return actorRole; }
    public void setActorRole(String v)          { this.actorRole = v; }
    public String getActorName()                { return actorName; }
    public void setActorName(String v)          { this.actorName = v; }
    public String getNote()                     { return note; }
    public void setNote(String v)               { this.note = v; }
    public Boolean getIsPublicNote()            { return isPublicNote; }
    public void setIsPublicNote(Boolean v)      { this.isPublicNote = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
}
