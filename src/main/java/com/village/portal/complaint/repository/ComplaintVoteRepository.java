package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ComplaintVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ComplaintVoteRepository extends JpaRepository<ComplaintVote, Long> {
    Optional<ComplaintVote> findByComplaintIdAndUserId(Long complaintId, Long userId);
    boolean existsByComplaintIdAndUserId(Long complaintId, Long userId);
    long countByComplaintId(Long complaintId);
}
