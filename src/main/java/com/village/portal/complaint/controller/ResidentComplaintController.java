package com.village.portal.complaint.controller;

import com.village.portal.complaint.dto.request.*;
import com.village.portal.complaint.dto.response.*;
import com.village.portal.complaint.service.ComplaintNotificationService;
import com.village.portal.complaint.service.ComplaintService;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

/**
 * Resident-facing complaint endpoints.
 * All require RESIDENT or ADMIN role.
 */
@RestController
@RequestMapping("/complaints")
@PreAuthorize("hasAnyRole('RESIDENT','ADMIN')")
public class ResidentComplaintController {

    private final ComplaintService             complaintService;
    private final ComplaintNotificationService notificationService;

    public ResidentComplaintController(ComplaintService complaintService,
                                        ComplaintNotificationService notificationService) {
        this.complaintService   = complaintService;
        this.notificationService = notificationService;
    }

    // ── Submit ────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> submit(
            @Valid @RequestBody SubmitComplaintRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.submit(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Complaint submitted successfully", result));
    }

    // ── Upload evidence (submission photos) ───────────────────

    @PostMapping("/{id}/evidence")
    public ResponseEntity<ApiResponse<Void>> uploadEvidence(
            @PathVariable Long id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        complaintService.uploadEvidence(id, files, currentUser.getId(), false);
        return ResponseEntity.ok(ApiResponse.success("Evidence uploaded successfully"));
    }

    // ── Vote / unvote ─────────────────────────────────────────

    @PostMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> vote(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        VoteResponse result = complaintService.toggleVote(id, currentUser.getId());
        String msg = result.isVoted() ? "Vote added" : "Vote removed";
        return ResponseEntity.ok(ApiResponse.success(msg, result));
    }

    // ── My complaints ─────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> myComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Page<ComplaintSummaryResponse> result = complaintService.getMyComplaints(
                currentUser.getId(),
                PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ── Resolution response ───────────────────────────────────

    @PostMapping("/{id}/resolution/accept")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> acceptResolution(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.acceptResolution(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Resolution accepted. Case closed.", result));
    }

    @PostMapping("/{id}/resolution/reject")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> rejectResolution(
            @PathVariable Long id,
            @Valid @RequestBody ResolutionResponseRequest request,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        ComplaintDetailResponse result = complaintService.rejectResolution(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Resolution rejected. Complaint re-opened.", result));
    }

    // ── Notifications ─────────────────────────────────────────

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> notifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Page<NotificationResponse> result = notificationService.getMyNotifications(
                currentUser.getId(),
                PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<ApiResponse<Long>> unreadCount(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        notificationService.markRead(notificationId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PatchMapping("/notifications/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        notificationService.markAllRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}
