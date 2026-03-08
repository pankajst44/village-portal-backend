package com.village.portal.service.impl;


import com.village.portal.aspect.Auditable;
import com.village.portal.dto.request.CreateFundRequest;
import com.village.portal.dto.response.FundResponse;
import com.village.portal.entity.Fund;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.FundStatus;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.DuplicateResourceException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.FundRepository;
import com.village.portal.repository.ProjectRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.FundService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FundServiceImpl implements FundService {

    private final FundRepository    fundRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository    userRepository;

    public FundServiceImpl(FundRepository fundRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository) {
        this.fundRepository    = fundRepository;
        this.projectRepository = projectRepository;
        this.userRepository    = userRepository;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, tableName = "funds",
            description = "New fund scheme created")
    public FundResponse createFund(CreateFundRequest request) {

        // Duplicate reference number check (only when one is supplied)
        if (request.getReferenceNumber() != null
                && !request.getReferenceNumber().isBlank()
                && fundRepository.existsByReferenceNumber(request.getReferenceNumber())) {
            throw new DuplicateResourceException(
                    "Fund with reference number '" + request.getReferenceNumber() + "' already exists");
        }

        Fund fund = new Fund();
        mapRequestToEntity(request, fund);
        fund.setCreatedBy(getCurrentUser());

        return toResponse(fundRepository.save(fund));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public FundResponse getFundById(Long id) {
        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund", "id", id));
        return toResponse(fund);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FundResponse> getAllFunds(Pageable pageable) {
        return fundRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundResponse> getFundsByStatus(FundStatus status) {
        return fundRepository.findByStatus(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FundResponse> getFundsByFinancialYear(String year) {
        return fundRepository.findByFinancialYear(year)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "funds",
            description = "Fund scheme updated")
    public FundResponse updateFund(Long id, CreateFundRequest request) {

        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund", "id", id));

        // If reference number is changing, check for conflicts
        String incomingRef = request.getReferenceNumber();
        if (incomingRef != null && !incomingRef.isBlank()) {
            boolean sameRecord = incomingRef.equals(fund.getReferenceNumber());
            if (!sameRecord && fundRepository.existsByReferenceNumber(incomingRef)) {
                throw new DuplicateResourceException(
                        "Fund with reference number '" + incomingRef + "' already exists");
            }
        }

        // Business rule: a CLOSED fund cannot be re-opened to PENDING
        if (fund.getStatus() == FundStatus.CLOSED
                && request.getStatus() == FundStatus.PENDING) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "A closed fund cannot be moved back to PENDING status");
        }

        mapRequestToEntity(request, fund);

        return toResponse(fundRepository.save(fund));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, tableName = "funds",
            description = "Fund scheme deleted")
    public void deleteFund(Long id) {

        Fund fund = fundRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fund", "id", id));

        // Business rule: cannot delete a fund that has linked projects
        boolean hasProjects = projectRepository.existsByFundId(id);
        if (hasProjects) {
            throw new BusinessException("HAS_LINKED_PROJECTS",
                    "Cannot delete a fund that has linked projects. Remove the project links first.");
        }

        fundRepository.delete(fund);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Copies all request fields onto the entity. Used by both create and update. */
    private void mapRequestToEntity(CreateFundRequest req, Fund fund) {
        fund.setSchemeNameEn(req.getSchemeNameEn());
        fund.setSchemeNameHi(req.getSchemeNameHi());
        fund.setFundSource(req.getFundSource());
        fund.setTotalAmount(req.getTotalAmount());
        fund.setAmountReceived(req.getAmountReceived() != null
                ? req.getAmountReceived()
                : java.math.BigDecimal.ZERO);
        fund.setReleaseDate(req.getReleaseDate());
        fund.setFinancialYear(req.getFinancialYear());
        fund.setReferenceNumber(req.getReferenceNumber());
        fund.setDescriptionEn(req.getDescriptionEn());
        fund.setDescriptionHi(req.getDescriptionHi());
        fund.setStatus(req.getStatus() != null ? req.getStatus() : FundStatus.PENDING);
    }

    /** Maps a Fund entity to its response DTO. */
    private FundResponse toResponse(Fund fund) {
        FundResponse r = new FundResponse();
        r.setId(fund.getId());
        r.setSchemeNameEn(fund.getSchemeNameEn());
        r.setSchemeNameHi(fund.getSchemeNameHi());
        r.setFundSource(fund.getFundSource());
        r.setTotalAmount(fund.getTotalAmount());
        r.setAmountReceived(fund.getAmountReceived());
        r.setReleaseDate(fund.getReleaseDate());
        r.setFinancialYear(fund.getFinancialYear());
        r.setReferenceNumber(fund.getReferenceNumber());
        r.setDescriptionEn(fund.getDescriptionEn());
        r.setDescriptionHi(fund.getDescriptionHi());
        r.setStatus(fund.getStatus());
        r.setCreatedAt(fund.getCreatedAt());
        r.setUpdatedAt(fund.getUpdatedAt());

        if (fund.getCreatedBy() != null) {
            r.setCreatedByUsername(fund.getCreatedBy().getUsername());
        }
        return r;
    }

    /** Reads the authenticated user from the Spring Security context. */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl details = (UserDetailsImpl) auth.getPrincipal();
            return userRepository.findById(details.getId()).orElse(null);
        }
        return null;
    }
}
