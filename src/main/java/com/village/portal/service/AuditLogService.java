package com.village.portal.service;

import com.village.portal.dto.response.AuditLogResponse;
import com.village.portal.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    // Manual log write (used for login/logout — AOP covers the rest)
    void log(String tableName, Long recordId, AuditAction action,
             String oldValues, String newValues, String summary);

    List<AuditLogResponse> getRecordHistory(String tableName, Long recordId);

    Page<AuditLogResponse> getAuditLogsByUser(Long userId, Pageable pageable);

    Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable);

    Page<AuditLogResponse> getAuditLogsByDateRange(
            LocalDateTime from, LocalDateTime to, Pageable pageable);
}
