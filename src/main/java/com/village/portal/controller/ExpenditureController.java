package com.village.portal.controller;

import com.village.portal.dto.request.CreateExpenditureRequest;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.ExpenditureResponse;
import com.village.portal.service.ExpenditureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenditures")
public class ExpenditureController {

    private final ExpenditureService expenditureService;

    public ExpenditureController(ExpenditureService expenditureService) {
        this.expenditureService = expenditureService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ExpenditureResponse>> recordExpenditure(
            @Valid @RequestBody CreateExpenditureRequest request) {

        ExpenditureResponse response = expenditureService.recordExpenditure(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Expenditure recorded successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<ExpenditureResponse>> getExpenditureById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(expenditureService.getExpenditureById(id)));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<Page<ExpenditureResponse>>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("paymentDate").descending());
        return ResponseEntity.ok(
                ApiResponse.success(expenditureService.getExpendituresByProject(projectId, pageable)));
    }

    @GetMapping("/project/{projectId}/date-range")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<List<ExpenditureResponse>>> getByProjectAndDateRange(
            @PathVariable Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(ApiResponse.success(
                expenditureService.getExpendituresByProjectAndDateRange(projectId, from, to)));
    }

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<ApiResponse<ExpenditureResponse>> verifyExpenditure(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Expenditure verified", expenditureService.verifyExpenditure(id)));
    }

    @GetMapping("/pending-verification")
    @PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
    public ResponseEntity<ApiResponse<List<ExpenditureResponse>>> getPendingVerifications() {
        return ResponseEntity.ok(
                ApiResponse.success(expenditureService.getPendingVerifications()));
    }
}
