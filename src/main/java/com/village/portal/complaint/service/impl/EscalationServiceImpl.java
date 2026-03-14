package com.village.portal.complaint.service.impl;


import com.village.portal.complaint.entity.Complaint;
import com.village.portal.complaint.entity.ComplaintTimeline;
import com.village.portal.complaint.entity.EscalationLog;
import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import com.village.portal.complaint.enums.EscalationTrigger;
import com.village.portal.complaint.enums.NotificationType;
import com.village.portal.complaint.repository.ComplaintRepository;
import com.village.portal.complaint.repository.ComplaintTimelineRepository;
import com.village.portal.complaint.repository.EscalationLogRepository;
import com.village.portal.complaint.service.ComplaintNotificationService;
import com.village.portal.complaint.service.EscalationService;
import com.village.portal.constants.AppConstants;
import com.village.portal.entity.User;
import com.village.portal.enums.Role;
import com.village.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EscalationServiceImpl implements EscalationService {

    private static final Logger log = LoggerFactory.getLogger(EscalationServiceImpl.class);

    private final ComplaintRepository            complaintRepository;
    private final EscalationLogRepository        escalationLogRepository;
    private final ComplaintTimelineRepository    timelineRepository;
    private final ComplaintNotificationService   notificationService;
    private final UserRepository                 userRepository;

    public EscalationServiceImpl(
            ComplaintRepository complaintRepository,
            EscalationLogRepository escalationLogRepository,
            ComplaintTimelineRepository timelineRepository,
            ComplaintNotificationService notificationService,
            UserRepository userRepository) {
        this.complaintRepository  = complaintRepository;
        this.escalationLogRepository = escalationLogRepository;
        this.timelineRepository   = timelineRepository;
        this.notificationService  = notificationService;
        this.userRepository       = userRepository;
    }

    // ── Manual or triggered escalation ───────────────────────

    @Override
    @Transactional
    public void escalate(Complaint complaint, EscalationTrigger trigger,
                         String note, Long escalatedByUserId) {

        int fromLevel = complaint.getEscalationLevel();
        int toLevel   = Math.min(fromLevel + 1, 3);

        if (toLevel == fromLevel) {
            log.debug("Complaint {} already at max escalation level 3 — skipping", complaint.getId());
            return;
        }

        complaint.setEscalationLevel(toLevel);
        complaint.setLastEscalatedAt(LocalDateTime.now());

        // Auto-upgrade priority to HIGH at level 2+
        if (toLevel >= 2 && complaint.getPriority() == ComplaintPriority.LOW) {
            complaint.setPriority(ComplaintPriority.MEDIUM);
        }
        if (toLevel >= 2 && complaint.getPriority() == ComplaintPriority.MEDIUM) {
            complaint.setPriority(ComplaintPriority.HIGH);
        }
        complaintRepository.save(complaint);

        // Find admin to notify
        List<User> admins = userRepository.findByRoleAndIsActive(Role.ADMIN, true);
        User targetAdmin  = admins.isEmpty() ? null : admins.get(0);

        // Log the escalation
        EscalationLog elog = new EscalationLog();
        elog.setComplaint(complaint);
        elog.setEscalatedFromLevel(fromLevel);
        elog.setEscalatedToLevel(toLevel);
        elog.setTriggerType(trigger);
        elog.setNote(note != null ? note : trigger.name());
        if (targetAdmin != null)       elog.setEscalatedToUser(targetAdmin);
        if (escalatedByUserId != null) {
            userRepository.findById(escalatedByUserId).ifPresent(elog::setEscalatedByUser);
        }
        escalationLogRepository.save(elog);

        // Notify admin
        if (targetAdmin != null) {
            String noteEn = "Complaint " + complaint.getComplaintNumber() + " escalated to level " + toLevel
                    + " — trigger: " + trigger.name();
            notificationService.send(targetAdmin.getId(), complaint,
                    NotificationType.ESCALATION_ALERT, noteEn, null);
        }

        // Notify assigned officer
        if (complaint.getAssignedOfficer() != null) {
            notificationService.send(complaint.getAssignedOfficer().getId(), complaint,
                    NotificationType.ESCALATION_ALERT,
                    "Your complaint " + complaint.getComplaintNumber() + " has been escalated.", null);
        }

        log.info("Complaint {} escalated from level {} to {} — trigger: {}",
                complaint.getComplaintNumber(), fromLevel, toLevel, trigger);
    }

    // ── Scheduled check ───────────────────────────────────────

    @Override
    @Transactional
    public void checkAndEscalateAll() {
        LocalDateTime inactivityThreshold = LocalDateTime.now()
                .minusDays(7); // default inactivity window

        List<Complaint> candidates = complaintRepository
                .findComplaintsForEscalationCheck(
                        List.of(ComplaintStatus.CLOSED, ComplaintStatus.REJECTED, ComplaintStatus.DRAFT),
                        inactivityThreshold);

        int escalated = 0;
        for (Complaint c : candidates) {
            try {
                EscalationTrigger trigger = determineTrigger(c);
                if (trigger != null) {
                    escalate(c, trigger, null, null);
                    escalated++;
                }
            } catch (Exception e) {
                log.error("Escalation check failed for complaint {}: {}",
                        c.getComplaintNumber(), e.getMessage());
            }
        }
        log.info("Escalation check complete — {} complaints processed, {} escalated",
                candidates.size(), escalated);
    }

    @Override
    @Transactional
    public void autoCloseExpiredResolutions() {
        List<Complaint> expired = complaintRepository
                .findExpiredResolutions(ComplaintStatus.RESOLVED, LocalDateTime.now());

        int closed = 0;
        for (Complaint c : expired) {
            try {
                c.setStatus(ComplaintStatus.CLOSED);
                c.setClosedAt(LocalDateTime.now());
                complaintRepository.save(c);

                // Append timeline entry so the audit trail is complete
                ComplaintTimeline entry = new ComplaintTimeline();
                entry.setComplaint(c);
                entry.setFromStatus(ComplaintStatus.RESOLVED);
                entry.setToStatus(ComplaintStatus.CLOSED);
                entry.setActorRole("SYSTEM");
                entry.setActorName("Automated Scheduler");
                entry.setNote("Auto-closed: citizen did not respond within "
                        + AppConstants.CITIZEN_RESOLUTION_RESPONSE_DAYS + " days.");
                entry.setIsPublicNote(true);
                timelineRepository.save(entry);

                // Notify the original submitter
                notificationService.send(
                        c.getSubmitter().getId(), c,
                        NotificationType.RESOLUTION_ACCEPTED,   // closest semantic type
                        "Your complaint " + c.getComplaintNumber()
                                + " has been automatically closed as no response was received within "
                                + AppConstants.CITIZEN_RESOLUTION_RESPONSE_DAYS + " days.",
                        null);

                log.info("Auto-closed complaint {} — deadline was {}",
                        c.getComplaintNumber(), c.getCitizenResponseDeadline());
                closed++;
            } catch (Exception e) {
                log.error("Auto-close failed for complaint {}: {}",
                        c.getComplaintNumber(), e.getMessage());
            }
        }
        log.info("Auto-close run complete — {} complaints closed", closed);
    }

    // ── Helpers ───────────────────────────────────────────────

    private EscalationTrigger determineTrigger(Complaint c) {
        // 1. SLA breach: due date passed and not resolved
        if (c.getDueDate() != null
                && c.getDueDate().isBefore(LocalDate.now())
                && c.getStatus() != ComplaintStatus.RESOLVED
                && c.getStatus() != ComplaintStatus.CLOSED) {
            return EscalationTrigger.SLA_BREACH;
        }

        // 2. Support threshold
        if (c.getSupportCount() >= AppConstants.SUPPORT_THRESHOLD_FOR_ESCALATION
                && c.getEscalationLevel() == 0) {
            return EscalationTrigger.SUPPORT_THRESHOLD;
        }

        // 3. Inactivity: no timeline entry in category's escalation_after_days
        int inactivityDays = c.getCategory() != null
                ? c.getCategory().getEscalationAfterDays() : 7;
        LocalDateTime activityCutoff = LocalDateTime.now().minusDays(inactivityDays);
        boolean hasRecentActivity = timelineRepository
                .existsByComplaintIdAndCreatedAtAfter(c.getId(), activityCutoff);
        if (!hasRecentActivity
                && c.getStatus() == ComplaintStatus.IN_PROGRESS) {
            return EscalationTrigger.INACTIVITY;
        }

        return null; // no escalation needed
    }
}