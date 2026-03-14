package com.village.portal.complaint.service;

import com.village.portal.complaint.dto.request.*;
import com.village.portal.complaint.dto.response.*;
import com.village.portal.complaint.enums.ComplaintPriority;
import com.village.portal.complaint.enums.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ComplaintService {

    // ── Resident ──────────────────────────────────────────────
    ComplaintDetailResponse submit(SubmitComplaintRequest request, Long submitterId);
    void uploadEvidence(Long complaintId, List<MultipartFile> files, Long uploaderId, boolean isResolutionProof);
    VoteResponse toggleVote(Long complaintId, Long userId);
    Page<ComplaintSummaryResponse> getMyComplaints(Long submitterId, Pageable pageable);
    ComplaintDetailResponse acceptResolution(Long complaintId, Long citizenId);
    ComplaintDetailResponse rejectResolution(Long complaintId, Long citizenId, ResolutionResponseRequest request);

    // ── Officer ───────────────────────────────────────────────
    Page<ComplaintSummaryResponse> getAssignedComplaints(Long officerId, List<ComplaintStatus> statuses, Pageable pageable);
    ComplaintDetailResponse postUpdate(Long complaintId, PostUpdateRequest request, Long officerId);
    ComplaintDetailResponse resolve(Long complaintId, ResolveComplaintRequest request, Long officerId);

    // ── Admin ─────────────────────────────────────────────────
    ComplaintDetailResponse verify(Long complaintId, String note, Long adminId);
    ComplaintDetailResponse reject(Long complaintId, RejectComplaintRequest request, Long adminId);
    ComplaintDetailResponse assign(Long complaintId, AssignComplaintRequest request, Long adminId);
    ComplaintDetailResponse escalate(Long complaintId, int level, String note, Long adminId);
    ComplaintDetailResponse changePriority(Long complaintId, ComplaintPriority priority, Long adminId);
    ComplaintDetailResponse toggleVisibility(Long complaintId, Long adminId);
    Page<ComplaintSummaryResponse> getAllForAdmin(ComplaintStatus status, ComplaintPriority priority,
                                                  Long categoryId, boolean escalatedOnly, Pageable pageable);
    ComplaintStatsResponse getStats(Long villageId);

    // ── Public ────────────────────────────────────────────────
    Page<ComplaintSummaryResponse> getPublicComplaints(Long villageId, ComplaintStatus status,
                                                        Long categoryId, Integer wardNumber,
                                                        String keyword, Pageable pageable);
    ComplaintDetailResponse getPublicDetail(String complaintNumber, Long viewerUserId);

    // ── Categories ────────────────────────────────────────────
    List<CategoryResponse> getActiveCategories();
}
