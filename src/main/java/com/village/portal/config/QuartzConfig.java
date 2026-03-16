package com.village.portal.config;

import com.village.portal.complaint.scheduler.AutoCloseJob;
import com.village.portal.complaint.scheduler.EscalationJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Value("${app.complaint.escalation.cron:0 0 * * * ?}")
    private String escalationCron;

    @Value("${app.complaint.autoclose.cron:0 0 2 * * ?}")
    private String autocloseCron;

    // ── Escalation job ────────────────────────────────────────

    @Bean
    public JobDetail escalationJobDetail() {
        return JobBuilder.newJob(EscalationJob.class)
                .withIdentity("escalationJob", "complaints")
                .withDescription("Hourly: escalate overdue / inactive complaints")
                .storeDurably()           // keep in DB even when no trigger references it
                .requestRecovery(true)    // re-fire if node crashed mid-execution
                .build();
    }

    @Bean
    public CronTrigger escalationTrigger(JobDetail escalationJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(escalationJobDetail)
                .withIdentity("escalationTrigger", "complaints")
                .withSchedule(
                    CronScheduleBuilder
                        .cronSchedule(escalationCron)
                        .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
                        .withMisfireHandlingInstructionDoNothing()  // skip missed fires, don't catch up
                )
                .build();
    }

    // ── Auto-close job ────────────────────────────────────────

    @Bean
    public JobDetail autoCloseJobDetail() {
        return JobBuilder.newJob(AutoCloseJob.class)
                .withIdentity("autoCloseJob", "complaints")
                .withDescription("Daily 2 AM IST: auto-close expired resolved complaints")
                .storeDurably()
                .requestRecovery(true)
                .build();
    }

    @Bean
    public CronTrigger autoCloseTrigger(JobDetail autoCloseJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(autoCloseJobDetail)
                .withIdentity("autoCloseTrigger", "complaints")
                .withSchedule(
                    CronScheduleBuilder
                        .cronSchedule(autocloseCron)
                        .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"))
                        .withMisfireHandlingInstructionFireAndProceed() // run once on recovery if missed
                )
                .build();
    }
}
