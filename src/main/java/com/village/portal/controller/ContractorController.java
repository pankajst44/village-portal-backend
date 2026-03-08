package com.village.portal.controller;
import com.village.portal.dto.request.BlacklistRequest;
import com.village.portal.dto.request.CreateContractorRequest;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.ContractorResponse;
import com.village.portal.enums.ContractorCategory;
import com.village.portal.service.ContractorService;
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
@RequestMapping("/contractors")
public class ContractorController {

    private final ContractorService contractorService;

    public ContractorController(ContractorService contractorService) {
        this.contractorService = contractorService;
    }

    // ── PUBLIC ENDPOINTS (no authentication required) ─────────────────────────

    /**
     * GET /contractors/public
     * Paginated list of active (non-blacklisted) contractors — citizen portal.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<ContractorResponse>>> getPublicContractors(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nameEn").ascending());
        return ResponseEntity.ok(ApiResponse.success(contractorService.getActiveContractors(pageable)));
    }

    /**
     * GET /contractors/public/{id}
     * Single contractor by ID — accessible without authentication.
     */
    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<ContractorResponse>> getPublicContractorById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contractorService.getContractorById(id)));
    }

    // ── AUTHENTICATED ENDPOINTS ───────────────────────────────────────────────

    /**
     * GET /contractors
     * Paginated list of ALL contractors (including blacklisted) — authenticated users.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<Page<ContractorResponse>>> getAllContractors(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nameEn").ascending());
        return ResponseEntity.ok(ApiResponse.success(contractorService.getAllContractors(pageable)));
    }

    /**
     * GET /contractors/{id}
     * Single contractor by ID — requires authentication.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<ContractorResponse>> getContractorById(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(contractorService.getContractorById(id)));
    }

    /**
     * GET /contractors/category/{category}
     * Filter contractors by category (CIVIL, ELECTRICAL, etc.) — requires authentication.
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<ContractorResponse>>> getContractorsByCategory(
            @PathVariable ContractorCategory category) {
        return ResponseEntity.ok(ApiResponse.success(
                contractorService.getContractorsByCategory(category)));
    }

    /**
     * GET /contractors/blacklisted
     * List all blacklisted contractors — Admin and Auditor only.
     */
    @GetMapping("/blacklisted")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<ApiResponse<List<ContractorResponse>>> getBlacklistedContractors() {
        return ResponseEntity.ok(ApiResponse.success(contractorService.getBlacklistedContractors()));
    }

    /**
     * POST /contractors
     * Register a new contractor — ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractorResponse>> createContractor(
            @Valid @RequestBody CreateContractorRequest request) {

        ContractorResponse response = contractorService.createContractor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Contractor registered successfully", response));
    }

    /**
     * PUT /contractors/{id}
     * Update contractor details — ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractorResponse>> updateContractor(
            @PathVariable Long id,
            @Valid @RequestBody CreateContractorRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Contractor updated successfully",
                contractorService.updateContractor(id, request)));
    }

    /**
     * PATCH /contractors/{id}/blacklist
     * Blacklist a contractor — ADMIN only.
     * Body: { "reason": "..." }
     */
    @PatchMapping("/{id}/blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractorResponse>> blacklistContractor(
            @PathVariable Long id,
            @Valid @RequestBody BlacklistRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                "Contractor blacklisted",
                contractorService.blacklistContractor(id, request.getReason())));
    }

    /**
     * PATCH /contractors/{id}/remove-blacklist
     * Remove a contractor from the blacklist — ADMIN only.
     */
    @PatchMapping("/{id}/remove-blacklist")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractorResponse>> removeFromBlacklist(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Contractor removed from blacklist",
                contractorService.removeFromBlacklist(id)));
    }

    /**
     * DELETE /contractors/{id}
     * Delete a contractor — ADMIN only.
     * Fails with 422 if the contractor has expenditures or project assignments.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteContractor(@PathVariable Long id) {
        contractorService.deleteContractor(id);
        return ResponseEntity.ok(ApiResponse.success("Contractor deleted successfully"));
    }
}
