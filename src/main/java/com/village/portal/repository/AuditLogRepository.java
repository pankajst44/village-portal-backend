package com.village.portal.repository;

import com.village.portal.entity.AuditLog;
import com.village.portal.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Full history of a single record
    List<AuditLog> findByTableNameAndRecordIdOrderByCreatedAtDesc(
            String tableName, Long recordId);

    // All actions by a user
    Page<AuditLog> findByChangedByOrderByCreatedAtDesc(Long changedBy, Pageable pageable);

    // Filter by action type
    Page<AuditLog> findByActionOrderByCreatedAtDesc(AuditAction action, Pageable pageable);

    // Date range audit search
    Page<AuditLog> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Table + date range
    Page<AuditLog> findByTableNameAndCreatedAtBetweenOrderByCreatedAtDesc(
            String tableName, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // Recent login events
    List<AuditLog> findByChangedByAndActionOrderByCreatedAtDesc(
            Long changedBy, AuditAction action);
}
