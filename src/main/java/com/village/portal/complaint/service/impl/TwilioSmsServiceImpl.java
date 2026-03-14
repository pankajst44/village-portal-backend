package com.village.portal.complaint.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.village.portal.complaint.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Twilio SMS implementation.
 *
 * Activated only when app.sms.provider=twilio (set in application.properties).
 * Falls back to LogSmsServiceImpl in dev when provider is not set or is 'log'.
 *
 * Free Twilio trial:
 *   - Sign up at https://www.twilio.com/try-twilio  (no credit card)
 *   - Get Account SID + Auth Token from Console Dashboard
 *   - Get a free trial phone number (From number)
 *   - Trial accounts can only send to verified numbers — add your test
 *     number at: Console → Phone Numbers → Verified Caller IDs
 */
@Service
@ConditionalOnProperty(name = "app.sms.provider", havingValue = "twilio")
public class TwilioSmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(TwilioSmsServiceImpl.class);

    @Value("${app.sms.twilio.account-sid}")
    private String accountSid;

    @Value("${app.sms.twilio.auth-token}")
    private String authToken;

    @Value("${app.sms.twilio.from-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
        log.info("Twilio SMS service initialised (from={})", fromNumber);
    }

    @Override
    public void send(String toPhone, String message) {
        try {
            // toPhone must be in E.164 format — formatPhone() handles Indian numbers
            String e164 = formatToE164(toPhone);

            Message msg = Message.creator(
                    new PhoneNumber(e164),
                    new PhoneNumber(fromNumber),
                    message
            ).create();

            log.info("SMS sent via Twilio | to={} | sid={} | status={}",
                    maskPhone(toPhone), msg.getSid(), msg.getStatus());

        } catch (Exception e) {
            // Never crash the OTP flow — log and rethrow as runtime so
            // ResidentRegistrationService can surface a friendly error
            log.error("Twilio SMS failed | to={} | error={}", maskPhone(toPhone), e.getMessage());
            throw new RuntimeException("SMS delivery failed. Please try again.", e);
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    /**
     * Convert a 10-digit Indian mobile number to E.164 (+91XXXXXXXXXX).
     * Passes through numbers already in E.164 format unchanged.
     */
    private String formatToE164(String phone) {
        if (phone == null) throw new IllegalArgumentException("Phone number is null");
        String cleaned = phone.replaceAll("[\\s\\-()]", "");
        if (cleaned.startsWith("+")) return cleaned;          // already E.164
        if (cleaned.startsWith("91") && cleaned.length() == 12) return "+" + cleaned;
        if (cleaned.length() == 10) return "+91" + cleaned;  // Indian 10-digit
        throw new IllegalArgumentException("Unrecognised phone format: " + maskPhone(phone));
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "****";
        return phone.substring(0, phone.length() - 4).replaceAll("\\d", "*")
                + phone.substring(phone.length() - 4);
    }
}
