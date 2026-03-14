package com.village.portal.complaint.controller;

import com.village.portal.complaint.dto.response.*;
import com.village.portal.complaint.enums.ComplaintStatus;
import com.village.portal.complaint.service.ComplaintService;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public endpoints — no authentication required.
 * Permitted via SecurityConfig: /complaints/public/**
 */
@RestController
@RequestMapping("/complaints/public")
public class PublicComplaintController {

    private final ComplaintService complaintService;

    public PublicComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ComplaintSummaryResponse>>> list(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer wardNumber,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Page<ComplaintSummaryResponse> result = complaintService.getPublicComplaints(
                null, status, categoryId, wardNumber, search,
                PageRequest.of(page, Math.min(size, 100), sort));

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{complaintNumber}")
    public ResponseEntity<ApiResponse<ComplaintDetailResponse>> detail(
            @PathVariable String complaintNumber,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {

        Long viewerId = currentUser != null ? currentUser.getId() : null;
        ComplaintDetailResponse detail = complaintService.getPublicDetail(complaintNumber, viewerId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> categories() {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getActiveCategories()));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ComplaintStatsResponse>> stats(
            @RequestParam(required = false) Long villageId) {
        return ResponseEntity.ok(ApiResponse.success(complaintService.getStats(villageId)));
    }
}
