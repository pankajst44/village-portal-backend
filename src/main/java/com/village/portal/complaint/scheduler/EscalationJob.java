package com.village.portal.complaint.scheduler;

import com.village.portal.complaint.service.EscalationService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz Job — runs on exactly ONE node in the cluster (DB lock ensures this).
 *
 * @DisallowConcurrentExecution prevents a second instance of this job from
 * firing on the same node if the previous run is still in progress.
 */
@Component
@DisallowConcurrentExecution
public class EscalationJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(EscalationJob.class);

    @Autowired
    private EscalationService escalationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            log.info("=== EscalationJob: Starting on node {} ===",
                    context.getScheduler().getSchedulerInstanceId());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        try {
            escalationService.checkAndEscalateAll();
        } catch (Exception e) {
            log.error("EscalationJob failed", e);
            // Wrap in JobExecutionException so Quartz records the failure
            throw new JobExecutionException(e);
        }
        log.info("=== EscalationJob: Complete ===");
    }
}
