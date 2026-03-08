package com.village.portal.service.impl;

import com.village.portal.dto.response.AuditLogResponse;
import com.village.portal.entity.AuditLog;
import com.village.portal.enums.AuditAction;
import com.village.portal.repository.AuditLogRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional
    public void log(String tableName, Long recordId, AuditAction action,
                    String oldValues, String newValues, String summary) {

        AuditLog auditLog = new AuditLog();
        auditLog.setTableName(tableName);
        auditLog.setRecordId(recordId);
        auditLog.setAction(action);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setChangeSummary(summary);

        // Extract authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
            auditLog.setChangedBy(user.getId());
            auditLog.setChangedByUsername(user.getUsername());
            auditLog.setChangedByRole(user.getRole());
        }

        // Extract IP
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xff = request.getHeader("X-Forwarded-For");
                auditLog.setIpAddress(xff != null ? xff.split(",")[0].trim()
                        : request.getRemoteAddr());
            }
        } catch (Exception ignored) {}

        auditLogRepository.save(auditLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecordHistory(String tableName, Long recordId) {
        return auditLogRepository
                .findByTableNameAndRecordIdOrderByCreatedAtDesc(tableName, recordId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByChangedByOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogsByDateRange(
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to, pageable)
                .map(this::toResponse);
    }

    // ── Mapper ──

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(log.getId());
        r.setTableName(log.getTableName());
        r.setRecordId(log.getRecordId());
        r.setAction(log.getAction());
        r.setChangedByUsername(log.getChangedByUsername());
        r.setChangedByRole(log.getChangedByRole());
        r.setIpAddress(log.getIpAddress());
        r.setOldValues(log.getOldValues());
        r.setNewValues(log.getNewValues());
        r.setChangeSummary(log.getChangeSummary());
        r.setCreatedAt(log.getCreatedAt());
        return r;
    }
}
