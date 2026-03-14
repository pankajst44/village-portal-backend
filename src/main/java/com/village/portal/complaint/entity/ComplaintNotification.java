package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.NotificationType;
import com.village.portal.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_notifications")
public class ComplaintNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 40)
    private NotificationType notificationType;

    @Column(name = "title_en", nullable = false, length = 200)
    private String titleEn;

    @Column(name = "title_hi", length = 200)
    private String titleHi;

    @Column(name = "message_en", nullable = false, columnDefinition = "TEXT")
    private String messageEn;

    @Column(name = "message_hi", columnDefinition = "TEXT")
    private String messageHi;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ComplaintNotification() {}

    public Long getId()                               { return id; }
    public User getUser()                             { return user; }
    public void setUser(User v)                       { this.user = v; }
    public Complaint getComplaint()                   { return complaint; }
    public void setComplaint(Complaint v)             { this.complaint = v; }
    public NotificationType getNotificationType()     { return notificationType; }
    public void setNotificationType(NotificationType v){ this.notificationType = v; }
    public String getTitleEn()                        { return titleEn; }
    public void setTitleEn(String v)                  { this.titleEn = v; }
    public String getTitleHi()                        { return titleHi; }
    public void setTitleHi(String v)                  { this.titleHi = v; }
    public String getMessageEn()                      { return messageEn; }
    public void setMessageEn(String v)                { this.messageEn = v; }
    public String getMessageHi()                      { return messageHi; }
    public void setMessageHi(String v)                { this.messageHi = v; }
    public Boolean getIsRead()                        { return isRead; }
    public void setIsRead(Boolean v)                  { this.isRead = v; }
    public LocalDateTime getReadAt()                  { return readAt; }
    public void setReadAt(LocalDateTime v)            { this.readAt = v; }
    public LocalDateTime getCreatedAt()               { return createdAt; }
}
