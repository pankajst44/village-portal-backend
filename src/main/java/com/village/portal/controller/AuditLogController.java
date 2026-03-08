package com.village.portal.controller;

import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.AuditLogResponse;
import com.village.portal.enums.AuditAction;
import com.village.portal.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/record/{tableName}/{recordId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getRecordHistory(
            @PathVariable String tableName,
            @PathVariable Long recordId) {

        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.getRecordHistory(tableName, recordId)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.getAuditLogsByUser(userId, pageable)));
    }

    @GetMapping("/action/{action}")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByAction(
            @PathVariable AuditAction action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.getAuditLogsByAction(action, pageable)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                ApiResponse.success(auditLogService.getAuditLogsByDateRange(from, to, pageable)));
    }
}
