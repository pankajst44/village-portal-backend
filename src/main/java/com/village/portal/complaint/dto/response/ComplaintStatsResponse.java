package com.village.portal.complaint.dto.response;

import java.util.Map;

public class ComplaintStatsResponse {
    private long total;
    private long submitted;
    private long verified;
    private long inProgress;
    private long resolved;
    private long closed;
    private long rejected;
    private long escalated;
    private long overdue;
    private Map<String, Long> byCategory;

    public long getTotal()                      { return total; }
    public void setTotal(long v)                { this.total = v; }
    public long getSubmitted()                  { return submitted; }
    public void setSubmitted(long v)            { this.submitted = v; }
    public long getVerified()                   { return verified; }
    public void setVerified(long v)             { this.verified = v; }
    public long getInProgress()                 { return inProgress; }
    public void setInProgress(long v)           { this.inProgress = v; }
    public long getResolved()                   { return resolved; }
    public void setResolved(long v)             { this.resolved = v; }
    public long getClosed()                     { return closed; }
    public void setClosed(long v)               { this.closed = v; }
    public long getRejected()                   { return rejected; }
    public void setRejected(long v)             { this.rejected = v; }
    public long getEscalated()                  { return escalated; }
    public void setEscalated(long v)            { this.escalated = v; }
    public long getOverdue()                    { return overdue; }
    public void setOverdue(long v)              { this.overdue = v; }
    public Map<String, Long> getByCategory()    { return byCategory; }
    public void setByCategory(Map<String, Long> v){ this.byCategory = v; }
}
