package com.village.portal.complaint.entity;

import com.village.portal.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_votes",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_vote_complaint_user",
           columnNames = {"complaint_id", "user_id"}))
public class ComplaintVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ComplaintVote() {}

    public Long getId()                   { return id; }
    public Complaint getComplaint()       { return complaint; }
    public void setComplaint(Complaint v) { this.complaint = v; }
    public User getUser()                 { return user; }
    public void setUser(User v)           { this.user = v; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
}
