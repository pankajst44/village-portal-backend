package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.EscalationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EscalationLogRepository extends JpaRepository<EscalationLog, Long> {
    List<EscalationLog> findByComplaintIdOrderByCreatedAtDesc(Long complaintId);
}
