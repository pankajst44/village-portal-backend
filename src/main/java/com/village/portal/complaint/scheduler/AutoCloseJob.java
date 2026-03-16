package com.village.portal.complaint.scheduler;

import com.village.portal.complaint.service.EscalationService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz Job — auto-closes RESOLVED complaints whose citizen response
 * deadline has expired. Runs daily at 02:00 IST on exactly one node.
 *
 * Misfire policy: FireAndProceed — if the server was down at 2 AM,
 * this job fires immediately on the next startup instead of being skipped.
 */
@Component
@DisallowConcurrentExecution
public class AutoCloseJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AutoCloseJob.class);

    @Autowired
    private EscalationService escalationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("=== AutoCloseJob: Starting on node {} ===",
                    context.getScheduler().getSchedulerInstanceId());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        try {
            escalationService.autoCloseExpiredResolutions();
        } catch (Exception e) {
            log.error("AutoCloseJob failed", e);
            throw new JobExecutionException(e);
        }
        log.info("=== AutoCloseJob: Complete ===");
    }
}
