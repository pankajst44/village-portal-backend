package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ComplaintTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComplaintTimelineRepository extends JpaRepository<ComplaintTimeline, Long> {

    // All public notes — for citizen/public view
    List<ComplaintTimeline> findByComplaintIdAndIsPublicNoteTrueOrderByCreatedAtAsc(Long complaintId);

    // All notes including private — for officer/admin
    List<ComplaintTimeline> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);

    // Last activity timestamp — for escalation inactivity check
    List<ComplaintTimeline> findTop1ByComplaintIdOrderByCreatedAtDesc(Long complaintId);

    // For inactivity check: any entry after given time
    boolean existsByComplaintIdAndCreatedAtAfter(Long complaintId, LocalDateTime after);
}
