package com.village.portal.complaint.service;

import com.village.portal.complaint.entity.Complaint;
import com.village.portal.complaint.enums.EscalationTrigger;

public interface EscalationService {
    void escalate(Complaint complaint, EscalationTrigger trigger, String note, Long escalatedByUserId);
    void checkAndEscalateAll();
    void autoCloseExpiredResolutions();
}
