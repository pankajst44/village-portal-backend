package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.ComplaintEvidence;
import com.village.portal.complaint.enums.EvidenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComplaintEvidenceRepository extends JpaRepository<ComplaintEvidence, Long> {

    // Public-visible evidence for a complaint
    List<ComplaintEvidence> findByComplaintIdAndIsPublicTrueAndIsDeletedFalseOrderByCreatedAtAsc(Long complaintId);

    // All evidence including private — for officer/admin
    List<ComplaintEvidence> findByComplaintIdAndIsDeletedFalseOrderByCreatedAtAsc(Long complaintId);

    // By type — e.g. RESOLUTION_PROOF only
    List<ComplaintEvidence> findByComplaintIdAndEvidenceTypeAndIsDeletedFalse(
            Long complaintId, EvidenceType evidenceType);

    // Count active (non-deleted) evidence for a complaint
    long countByComplaintIdAndIsDeletedFalse(Long complaintId);
}
