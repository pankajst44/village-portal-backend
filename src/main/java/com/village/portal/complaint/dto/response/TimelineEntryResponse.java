package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.ComplaintStatus;
import java.time.LocalDateTime;

public class TimelineEntryResponse {
    private Long id;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String actorRole;
    private String actorName;
    private String note;
    private Boolean isPublicNote;
    private LocalDateTime createdAt;

    public Long getId()                         { return id; }
    public void setId(Long v)                   { this.id = v; }
    public ComplaintStatus getFromStatus()      { return fromStatus; }
    public void setFromStatus(ComplaintStatus v){ this.fromStatus = v; }
    public ComplaintStatus getToStatus()        { return toStatus; }
    public void setToStatus(ComplaintStatus v)  { this.toStatus = v; }
    public String getActorRole()                { return actorRole; }
    public void setActorRole(String v)          { this.actorRole = v; }
    public String getActorName()                { return actorName; }
    public void setActorName(String v)          { this.actorName = v; }
    public String getNote()                     { return note; }
    public void setNote(String v)               { this.note = v; }
    public Boolean getIsPublicNote()            { return isPublicNote; }
    public void setIsPublicNote(Boolean v)      { this.isPublicNote = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public void setCreatedAt(LocalDateTime v)   { this.createdAt = v; }
}
