package com.village.portal.complaint.service.impl;

import com.village.portal.complaint.dto.response.NotificationResponse;
import com.village.portal.complaint.entity.Complaint;
import com.village.portal.complaint.entity.ComplaintNotification;
import com.village.portal.complaint.enums.NotificationType;
import com.village.portal.complaint.repository.ComplaintNotificationRepository;
import com.village.portal.complaint.service.ComplaintNotificationService;
import com.village.portal.entity.User;
import com.village.portal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintNotificationServiceImpl implements ComplaintNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintNotificationServiceImpl.class);

    private final ComplaintNotificationRepository notificationRepository;
    private final UserRepository                  userRepository;

    public ComplaintNotificationServiceImpl(
            ComplaintNotificationRepository notificationRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository         = userRepository;
    }

    @Override
    @Async
    @Transactional
    public void send(Long recipientUserId, Complaint complaint,
                     NotificationType type, String noteEn, String noteHi) {
        try {
            User recipient = userRepository.findById(recipientUserId).orElse(null);
            if (recipient == null) {
                log.warn("Notification skipped — user {} not found", recipientUserId);
                return;
            }

            String[] titles   = buildTitle(type, complaint);
            String[] messages = buildMessage(type, complaint, noteEn, noteHi);

            ComplaintNotification n = new ComplaintNotification();
            n.setUser(recipient);
            n.setComplaint(complaint);
            n.setNotificationType(type);
            n.setTitleEn(titles[0]);
            n.setTitleHi(titles[1]);
            n.setMessageEn(messages[0]);
            n.setMessageHi(messages[1]);
            n.setIsRead(false);
            notificationRepository.save(n);
        } catch (Exception e) {
            log.error("Failed to send notification type={} to user={}: {}",
                    type, recipientUserId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    // ── Message builders ──────────────────────────────────────

    private String[] buildTitle(NotificationType type, Complaint c) {
        String num = c.getComplaintNumber();
        return switch (type) {
            case COMPLAINT_SUBMITTED  -> new String[]{"Complaint Submitted",            "शिकायत दर्ज"};
            case COMPLAINT_VERIFIED   -> new String[]{"Complaint Verified: " + num,     "शिकायत सत्यापित: " + num};
            case COMPLAINT_REJECTED   -> new String[]{"Complaint Rejected: " + num,     "शिकायत अस्वीकृत: " + num};
            case COMPLAINT_ASSIGNED   -> new String[]{"Complaint Assigned: " + num,     "शिकायत सौंपी गई: " + num};
            case OFFICER_UPDATE       -> new String[]{"Update on " + num,               num + " पर अपडेट"};
            case RESOLUTION_READY     -> new String[]{"Resolution Ready: " + num,       "समाधान तैयार: " + num};
            case RESOLUTION_ACCEPTED  -> new String[]{"Resolution Accepted: " + num,    "समाधान स्वीकृत: " + num};
            case RESOLUTION_REJECTED  -> new String[]{"Resolution Rejected: " + num,    "समाधान अस्वीकृत: " + num};
            case ESCALATION_ALERT     -> new String[]{"Escalation Alert: " + num,       "एस्केलेशन अलर्ट: " + num};
        };
    }

    private String[] buildMessage(NotificationType type, Complaint c,
                                   String noteEn, String noteHi) {
        String num = c.getComplaintNumber();
        String nh  = noteHi != null ? noteHi : (noteEn != null ? noteEn : "");
        String ne  = noteEn != null ? noteEn : "";
        return switch (type) {
            case COMPLAINT_SUBMITTED ->
                new String[]{"Your complaint " + num + " has been submitted and is under review.",
                             "आपकी शिकायत " + num + " दर्ज हो गई है और समीक्षा में है।"};
            case COMPLAINT_VERIFIED ->
                new String[]{"Your complaint " + num + " has been verified and will be assigned to an officer.",
                             "आपकी शिकायत " + num + " सत्यापित हो गई है।"};
            case COMPLAINT_REJECTED ->
                new String[]{"Your complaint " + num + " was rejected. Reason: " + ne,
                             "आपकी शिकायत " + num + " अस्वीकृत हुई। कारण: " + nh};
            case COMPLAINT_ASSIGNED ->
                new String[]{"Complaint " + num + " has been assigned to you for resolution.",
                             "शिकायत " + num + " आपको सौंपी गई है।"};
            case OFFICER_UPDATE ->
                new String[]{"Officer update on " + num + ": " + ne,
                             num + " पर अधिकारी अपडेट: " + nh};
            case RESOLUTION_READY ->
                new String[]{"Complaint " + num + " has been resolved. Please review and accept or reject within 7 days.",
                             "शिकायत " + num + " का समाधान हो गया है। 7 दिनों में स्वीकार या अस्वीकार करें।"};
            case RESOLUTION_ACCEPTED ->
                new String[]{"Citizen accepted the resolution for " + num + ". Case closed.",
                             "नागरिक ने " + num + " का समाधान स्वीकार किया।"};
            case RESOLUTION_REJECTED ->
                new String[]{"Citizen rejected resolution for " + num + ". Reason: " + ne,
                             "नागरिक ने " + num + " का समाधान अस्वीकार किया। कारण: " + nh};
            case ESCALATION_ALERT ->
                new String[]{"Complaint " + num + " has been escalated. " + ne,
                             "शिकायत " + num + " एस्केलेट हुई। " + nh};
        };
    }

    private NotificationResponse toResponse(ComplaintNotification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        if (n.getComplaint() != null) {
            r.setComplaintId(n.getComplaint().getId());
            r.setComplaintNumber(n.getComplaint().getComplaintNumber());
        }
        r.setNotificationType(n.getNotificationType());
        r.setTitleEn(n.getTitleEn());
        r.setTitleHi(n.getTitleHi());
        r.setMessageEn(n.getMessageEn());
        r.setMessageHi(n.getMessageHi());
        r.setIsRead(n.getIsRead());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
