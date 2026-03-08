package com.village.portal.controller;

import com.village.portal.dto.request.CreateFundRequest;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.FundResponse;
import com.village.portal.enums.FundStatus;
import com.village.portal.service.FundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/funds")
public class FundController {

    private final FundService fundService;

    public FundController(FundService fundService) {
        this.fundService = fundService;
    }

    // ── PUBLIC ENDPOINTS (no authentication required) ─────────────────────────

    /**
     * GET /funds/public
     * Paginated list of all funds — accessible by unauthenticated users (citizen portal).
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<FundResponse>>> getPublicFunds(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(fundService.getAllFunds(pageable)));
    }

    /**
     * GET /funds/public/{id}
     * Single fund by ID — accessible by unauthenticated users.
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<FundResponse>> getPublicFundById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fundService.getFundById(id)));
    }

    /**
     * GET /funds/public/status/{status}
     * Funds filtered by status (e.g. ACTIVE) — used by dashboard without auth.
     */
    @GetMapping("/public/status/{status}")
    public ResponseEntity<ApiResponse<List<FundResponse>>> getPublicFundsByStatus(
            @PathVariable FundStatus status) {
        return ResponseEntity.ok(ApiResponse.success(fundService.getFundsByStatus(status)));
    }

    // ── AUTHENTICATED ENDPOINTS ───────────────────────────────────────────────

    /**
     * GET /funds
     * Paginated list — requires authentication.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<Page<FundResponse>>> getAllFunds(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(fundService.getAllFunds(pageable)));
    }

    /**
     * GET /funds/{id}
     * Single fund by ID — requires authentication.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<FundResponse>> getFundById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(fundService.getFundById(id)));
    }

    /**
     * GET /funds/status/{status}
     * Funds filtered by status — requires authentication.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<FundResponse>>> getFundsByStatus(
            @PathVariable FundStatus status) {
        return ResponseEntity.ok(ApiResponse.success(fundService.getFundsByStatus(status)));
    }

    /**
     * GET /funds/year/{year}
     * Funds for a specific financial year (e.g. 2024-25) — requires authentication.
     */
    @GetMapping("/year/{year}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<FundResponse>>> getFundsByFinancialYear(
            @PathVariable String year) {
        return ResponseEntity.ok(ApiResponse.success(fundService.getFundsByFinancialYear(year)));
    }

    /**
     * POST /funds
     * Create a new fund scheme — ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FundResponse>> createFund(
            @Valid @RequestBody CreateFundRequest request) {

        FundResponse response = fundService.createFund(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fund created successfully", response));
    }

    /**
     * PUT /funds/{id}
     * Update an existing fund scheme — ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FundResponse>> updateFund(
            @PathVariable Long id,
            @Valid @RequestBody CreateFundRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Fund updated successfully", fundService.updateFund(id, request)));
    }

    /**
     * DELETE /funds/{id}
     * Delete a fund scheme — ADMIN only.
     * Fails with 422 if any project is still linked to this fund.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFund(@PathVariable Long id) {
        fundService.deleteFund(id);
        return ResponseEntity.ok(ApiResponse.success("Fund deleted successfully"));
    }
}
