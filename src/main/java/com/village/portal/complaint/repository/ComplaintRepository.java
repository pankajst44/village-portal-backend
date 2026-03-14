package com.village.portal.complaint.repository;

import com.village.portal.complaint.entity.Complaint;
import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    // Public listing — village + optional filters
    @Query("SELECT c FROM Complaint c" +
           " WHERE c.village.id = :villageId" +
           " AND c.isPublic = true" +
           " AND (:status IS NULL OR c.status = :status)" +
           " AND (:categoryId IS NULL OR c.category.id = :categoryId)" +
           " AND (:wardNumber IS NULL OR c.wardNumber = :wardNumber)" +
           " ORDER BY c.createdAt DESC")
    Page<Complaint> findPublicComplaints(
            @Param("villageId") Long villageId,
            @Param("status") ComplaintStatus status,
            @Param("categoryId") Long categoryId,
            @Param("wardNumber") Integer wardNumber,
            Pageable pageable);

    // Full-text search via MySQL FULLTEXT index
    @Query(value = "SELECT * FROM complaints" +
                   " WHERE village_id = :villageId" +
                   " AND is_public = 1" +
                   " AND MATCH(title_en, description_en) AGAINST (:keyword IN BOOLEAN MODE)" +
                   " ORDER BY created_at DESC",
           countQuery = "SELECT COUNT(*) FROM complaints" +
                        " WHERE village_id = :villageId" +
                        " AND is_public = 1" +
                        " AND MATCH(title_en, description_en) AGAINST (:keyword IN BOOLEAN MODE)",
           nativeQuery = true)
    Page<Complaint> searchPublicComplaints(
            @Param("villageId") Long villageId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // Officer's assigned complaints
    Page<Complaint> findByAssignedOfficerIdAndStatusIn(
            Long officerId, List<ComplaintStatus> statuses, Pageable pageable);

    // Admin — all complaints with optional filters
    @Query("SELECT c FROM Complaint c" +
           " WHERE (:status IS NULL OR c.status = :status)" +
           " AND (:priority IS NULL OR c.priority = :priority)" +
           " AND (:categoryId IS NULL OR c.category.id = :categoryId)" +
           " AND (:escalatedOnly = false OR c.escalationLevel > 0)")
    Page<Complaint> findAllForAdmin(
            @Param("status") ComplaintStatus status,
            @Param("priority") ComplaintPriority priority,
            @Param("categoryId") Long categoryId,
            @Param("escalatedOnly") boolean escalatedOnly,
            Pageable pageable);

    // Submitter's own complaints
    Page<Complaint> findBySubmitterIdOrderByCreatedAtDesc(Long submitterId, Pageable pageable);

    // Duplicate detection
    @Query("SELECT c FROM Complaint c" +
           " WHERE c.submitter.id = :submitterId" +
           " AND c.category.id = :categoryId" +
           " AND c.wardNumber = :wardNumber" +
           " AND c.status NOT IN :excludedStatuses" +
           " AND c.createdAt >= :since")
    List<Complaint> findPotentialDuplicates(
            @Param("submitterId") Long submitterId,
            @Param("categoryId") Long categoryId,
            @Param("wardNumber") Integer wardNumber,
            @Param("excludedStatuses") List<ComplaintStatus> excludedStatuses,
            @Param("since") LocalDateTime since);

    // Rate limiting: count submitted complaints in last 24 hours
    @Query("SELECT COUNT(c) FROM Complaint c" +
           " WHERE c.submitter.id = :submitterId" +
           " AND c.createdAt >= :since" +
           " AND c.status <> :draftStatus")
    long countRecentBySubmitter(@Param("submitterId") Long submitterId,
                                @Param("since") LocalDateTime since,
                                @Param("draftStatus") ComplaintStatus draftStatus);

    // Escalation scheduler candidates
    @Query("SELECT c FROM Complaint c" +
           " WHERE c.status NOT IN :excludedStatuses" +
           " AND (c.dueDate IS NOT NULL AND c.dueDate < CURRENT_DATE" +
           "      OR c.lastEscalatedAt IS NULL" +
           "      OR c.lastEscalatedAt < :inactivityThreshold)")
    List<Complaint> findComplaintsForEscalationCheck(
            @Param("excludedStatuses") List<ComplaintStatus> excludedStatuses,
            @Param("inactivityThreshold") LocalDateTime inactivityThreshold);

    // Stats grouped by status for a village
    @Query("SELECT c.status, COUNT(c) FROM Complaint c" +
           " WHERE c.village.id = :villageId AND c.isPublic = true" +
           " GROUP BY c.status")
    List<Object[]> countByStatusForVillage(@Param("villageId") Long villageId);
    // Auto-close: resolved complaints where citizen response deadline has passed
    @Query("SELECT c FROM Complaint c" +
            " WHERE c.status = :resolvedStatus" +
            " AND c.citizenResponseDeadline IS NOT NULL" +
            " AND c.citizenResponseDeadline < :now")
    List<Complaint> findExpiredResolutions(
            @Param("resolvedStatus") ComplaintStatus resolvedStatus,
            @Param("now") LocalDateTime now);
}
