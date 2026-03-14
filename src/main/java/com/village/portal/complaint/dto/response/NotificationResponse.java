package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponse {
    private Long id;
    private Long complaintId;
    private String complaintNumber;
    private NotificationType notificationType;
    private String titleEn;
    private String titleHi;
    private String messageEn;
    private String messageHi;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public Long getId()                                 { return id; }
    public void setId(Long v)                           { this.id = v; }
    public Long getComplaintId()                        { return complaintId; }
    public void setComplaintId(Long v)                  { this.complaintId = v; }
    public String getComplaintNumber()                  { return complaintNumber; }
    public void setComplaintNumber(String v)            { this.complaintNumber = v; }
    public NotificationType getNotificationType()       { return notificationType; }
    public void setNotificationType(NotificationType v) { this.notificationType = v; }
    public String getTitleEn()                          { return titleEn; }
    public void setTitleEn(String v)                    { this.titleEn = v; }
    public String getTitleHi()                          { return titleHi; }
    public void setTitleHi(String v)                    { this.titleHi = v; }
    public String getMessageEn()                        { return messageEn; }
    public void setMessageEn(String v)                  { this.messageEn = v; }
    public String getMessageHi()                        { return messageHi; }
    public void setMessageHi(String v)                  { this.messageHi = v; }
    public Boolean getIsRead()                          { return isRead; }
    public void setIsRead(Boolean v)                    { this.isRead = v; }
    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime v)           { this.createdAt = v; }
}
