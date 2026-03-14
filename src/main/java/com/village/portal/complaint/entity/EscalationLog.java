package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.EscalationTrigger;
import com.village.portal.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_log")
public class EscalationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(name = "escalated_from_level", nullable = false)
    private Integer escalatedFromLevel;

    @Column(name = "escalated_to_level", nullable = false)
    private Integer escalatedToLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 30)
    private EscalationTrigger triggerType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_to_user_id")
    private User escalatedToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escalated_by_user_id")
    private User escalatedByUser;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public EscalationLog() {}

    public Long getId()                               { return id; }
    public Complaint getComplaint()                   { return complaint; }
    public void setComplaint(Complaint v)             { this.complaint = v; }
    public Integer getEscalatedFromLevel()            { return escalatedFromLevel; }
    public void setEscalatedFromLevel(Integer v)      { this.escalatedFromLevel = v; }
    public Integer getEscalatedToLevel()              { return escalatedToLevel; }
    public void setEscalatedToLevel(Integer v)        { this.escalatedToLevel = v; }
    public EscalationTrigger getTriggerType()         { return triggerType; }
    public void setTriggerType(EscalationTrigger v)   { this.triggerType = v; }
    public User getEscalatedToUser()                  { return escalatedToUser; }
    public void setEscalatedToUser(User v)            { this.escalatedToUser = v; }
    public User getEscalatedByUser()                  { return escalatedByUser; }
    public void setEscalatedByUser(User v)            { this.escalatedByUser = v; }
    public String getNote()                           { return note; }
    public void setNote(String v)                     { this.note = v; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
}
