package com.village.portal.complaint.controller;

import com.village.portal.complaint.dto.request.PostUpdateRequest;
import com.village.portal.complaint.dto.request.ResolveComplaintRequest;
import com.village.portal.complaint.dto.response.ComplaintDetailResponse;
import com.village.portal.complaint.dto.response.ComplaintSummaryResponse;
import com.village.portal.complaint.enums.ComplaintStatus;
import com.village.portal.complaint.service.ComplaintService;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * Officer-facing complaint endpoints.
 * Requires OFFICER or ADMIN role.
 */
@RestController
@RequestMapping("/complaints")
@PreAuthorize("hasAnyRole('OFFICER','ADMIN')")
public class OfficerComplaintController {

    private final ComplaintService complaintService;

    public OfficerComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ── Assigned queue ────────────────────────────────────────

    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> assigned(
            @RequestParam(required = false) List<ComplaintStatus> statuses,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Page<ComplaintSummaryResponse> result = complaintService.getAssignedComplaints(
                currentUser.getId(), statuses,
                PageRequest.of(page, Math.min(size, 100), Sort.by("dueDate").ascending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Post a status update or note ──────────────────────────

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> postUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.postUpdate(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Update posted", result));
    }

    // ── Upload resolution proof ───────────────────────────────

    @PostMapping("/{id}/evidence/resolution")
    public ResponseEntity<ApiResponse<Void>> uploadResolutionProof(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        complaintService.uploadEvidence(id, files, currentUser.getId(), true);
        return ResponseEntity.ok(ApiResponse.success("Resolution proof uploaded"));
    }

    // ── Mark as resolved ──────────────────────────────────────

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ResolveComplaintRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.resolve(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complaint marked as resolved", result));
    }
}
