package com.village.portal.complaint.service;

import com.village.portal.complaint.entity.Complaint;
import com.village.portal.complaint.enums.NotificationType;
import com.village.portal.complaint.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintNotificationService {
    void send(Long recipientUserId, Complaint complaint, NotificationType type, String noteEn, String noteHi);
    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);
    long getUnreadCount(Long userId);
    void markRead(Long notificationId, Long userId);
    void markAllRead(Long userId);
}
