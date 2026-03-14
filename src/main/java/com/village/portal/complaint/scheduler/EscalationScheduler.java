package com.village.portal.complaint.scheduler;

import com.village.portal.complaint.service.EscalationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EscalationScheduler {

    private static final Logger log = LoggerFactory.getLogger(EscalationScheduler.class);

    private final EscalationService escalationService;

    public EscalationScheduler(EscalationService escalationService) {
        this.escalationService = escalationService;
    }

    /**
     * Hourly escalation check.
     * Finds complaints that are overdue (SLA breach), inactive, or have hit
     * the community support threshold — and escalates each one.
     */
    @Scheduled(cron = "${app.complaint.escalation.cron:0 0 * * * *}")
    public void runEscalationCheck() {
        log.info("=== EscalationScheduler: Starting hourly escalation check ===");
        try {
            escalationService.checkAndEscalateAll();
        } catch (Exception e) {
            log.error("EscalationScheduler: Unexpected error during escalation check", e);
        }
        log.info("=== EscalationScheduler: Escalation check complete ===");
    }

    /**
     * Daily auto-close at 02:00 AM.
     * Finds RESOLVED complaints where the citizen's response deadline has
     * passed without an accept or reject — automatically closes them.
     */
    @Scheduled(cron = "${app.complaint.autoclose.cron:0 0 2 * * *}")
    public void runAutoClose() {
        log.info("=== EscalationScheduler: Starting daily auto-close run ===");
        try {
            escalationService.autoCloseExpiredResolutions();
        } catch (Exception e) {
            log.error("EscalationScheduler: Unexpected error during auto-close run", e);
        }
        log.info("=== EscalationScheduler: Auto-close run complete ===");
    }
}