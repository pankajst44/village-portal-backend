package com.village.portal.complaint.controller;

import com.village.portal.complaint.dto.request.AssignComplaintRequest;
import com.village.portal.complaint.dto.request.PostUpdateRequest;
import com.village.portal.complaint.dto.request.RejectComplaintRequest;
import com.village.portal.complaint.dto.response.ComplaintDetailResponse;
import com.village.portal.complaint.dto.response.ComplaintStatsResponse;
import com.village.portal.complaint.dto.response.ComplaintSummaryResponse;
import com.village.portal.complaint.enums.ComplaintPriority;
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

import javax.validation.Valid;

/**
 * Admin-only complaint management endpoints.
 * Requires ADMIN role. AUDITOR can access GET endpoints via separate security rules.
 */
@RestController
@RequestMapping("/complaints/admin")
@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
public class AdminComplaintController {

    private final ComplaintService complaintService;

    public AdminComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    // ── List all ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> listAll(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) ComplaintPriority priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "false") boolean escalatedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<ComplaintSummaryResponse> result = complaintService.getAllForAdmin(
                status, priority, categoryId, escalatedOnly,
                PageRequest.of(page, Math.min(size, 100), sort));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Stats ─────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> stats(
            @RequestParam(required = false) Long villageId) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getStats(villageId)));
    }

    // ── Verify ────────────────────────────────────────────────

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> verify(
            @PathVariable Long id,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.verify(id, note, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complaint verified", result));
    }

    // ── Reject ────────────────────────────────────────────────

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectComplaintRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.reject(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complaint rejected", result));
    }

    // ── Assign ────────────────────────────────────────────────

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> assign(
            @PathVariable Long id,
            @Valid @RequestBody AssignComplaintRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.assign(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complaint assigned", result));
    }

    // ── Escalate ──────────────────────────────────────────────

    @PostMapping("/{id}/escalate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> escalate(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int level,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.escalate(id, level, note, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Complaint escalated to level " + level, result));
    }

    // ── Change priority ───────────────────────────────────────

    @PatchMapping("/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> changePriority(
            @PathVariable Long id,
            @RequestParam ComplaintPriority priority,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.changePriority(id, priority, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Priority updated to " + priority, result));
    }

    // ── Toggle visibility ─────────────────────────────────────

    @PatchMapping("/{id}/visibility")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> toggleVisibility(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.toggleVisibility(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Visibility toggled", result));
    }
}
