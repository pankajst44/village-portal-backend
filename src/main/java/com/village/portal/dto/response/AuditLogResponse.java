package com.village.portal.dto.response;

import com.village.portal.enums.AuditAction;
import java.time.LocalDateTime;

public class AuditLogResponse {

    private Long id;
    private String tableName;
    private Long recordId;
    private AuditAction action;
    private String changedByUsername;
    private String changedByRole;
    private String ipAddress;
    private String oldValues;
    private String newValues;
    private String changeSummary;
    private LocalDateTime createdAt;

    public AuditLogResponse() {}

    public Long getId()                          { return id; }
    public void setId(Long v)                    { this.id = v; }

    public String getTableName()                 { return tableName; }
    public void setTableName(String v)           { this.tableName = v; }

    public Long getRecordId()                    { return recordId; }
    public void setRecordId(Long v)              { this.recordId = v; }

    public AuditAction getAction()               { return action; }
    public void setAction(AuditAction v)         { this.action = v; }

    public String getChangedByUsername()         { return changedByUsername; }
    public void setChangedByUsername(String v)   { this.changedByUsername = v; }

    public String getChangedByRole()             { return changedByRole; }
    public void setChangedByRole(String v)       { this.changedByRole = v; }

    public String getIpAddress()                 { return ipAddress; }
    public void setIpAddress(String v)           { this.ipAddress = v; }

    public String getOldValues()                 { return oldValues; }
    public void setOldValues(String v)           { this.oldValues = v; }

    public String getNewValues()                 { return newValues; }
    public void setNewValues(String v)           { this.newValues = v; }

    public String getChangeSummary()             { return changeSummary; }
    public void setChangeSummary(String v)       { this.changeSummary = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
}
