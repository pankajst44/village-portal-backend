package com.village.portal.complaint.service.impl;

import com.village.portal.complaint.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Development fallback — prints the SMS to the console instead of sending it.
 * Active when app.sms.provider=log (or when the property is not set at all).
 *
 * Look for this in your Spring Boot console:
 *   [SMS-LOG] ➜  To: +919876543210  |  "Village Portal OTP: 482910  ..."
 */
@Service
@ConditionalOnProperty(
        name = "app.sms.provider",
        havingValue = "log",
        matchIfMissing = true   // ← default when property is absent
)
public class LogSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(LogSmsServiceImpl.class);

    @Override
    public void send(String toPhone, String message) {
        log.warn("╔══════════════════════════════════════════════════╗");
        log.warn("║  [SMS-LOG]  No real SMS sent — dev mode          ║");
        log.warn("║  To      : {}                     ║", toPhone);
        log.warn("║  Message : {}  ║", message);
        log.warn("╚══════════════════════════════════════════════════╝");
    }
}
