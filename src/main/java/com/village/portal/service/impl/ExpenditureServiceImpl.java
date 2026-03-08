package com.village.portal.service.impl;

import com.village.portal.aspect.Auditable;
import com.village.portal.dto.request.CreateExpenditureRequest;
import com.village.portal.dto.response.ExpenditureResponse;
import com.village.portal.entity.Contractor;
import com.village.portal.entity.Expenditure;
import com.village.portal.entity.Project;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.ContractorRepository;
import com.village.portal.repository.ExpenditureRepository;
import com.village.portal.repository.ProjectRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.ExpenditureService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenditureServiceImpl implements ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final ProjectRepository projectRepository;
    private final ContractorRepository contractorRepository;
    private final UserRepository userRepository;

    public ExpenditureServiceImpl(ExpenditureRepository expenditureRepository,
                                   ProjectRepository projectRepository,
                                   ContractorRepository contractorRepository,
                                   UserRepository userRepository) {
        this.expenditureRepository = expenditureRepository;
        this.projectRepository     = projectRepository;
        this.contractorRepository  = contractorRepository;
        this.userRepository        = userRepository;
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, tableName = "expenditures",
               description = "Expenditure recorded")
    public ExpenditureResponse recordExpenditure(CreateExpenditureRequest request) {

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        // ── BUSINESS RULE: Spending cannot exceed allocated budget ──
        BigDecimal projectedTotal = project.getTotalSpent().add(request.getAmount());
        if (projectedTotal.compareTo(project.getAllocatedBudget()) > 0) {
            throw new BusinessException("BUDGET_EXCEEDED",
                    String.format("This payment of ₹%.2f would exceed the allocated budget. " +
                                  "Remaining budget: ₹%.2f",
                            request.getAmount(),
                            project.getAllocatedBudget().subtract(project.getTotalSpent())));
        }

        // ── BUSINESS RULE: Contractor must not be blacklisted ──
        Contractor contractor = null;
        if (request.getContractorId() != null) {
            contractor = contractorRepository.findById(request.getContractorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", request.getContractorId()));
            if (Boolean.TRUE.equals(contractor.getIsBlacklisted())) {
                throw new BusinessException("CONTRACTOR_BLACKLISTED",
                        "Cannot record payment to a blacklisted contractor: "
                                + contractor.getNameEn());
            }
        }

        Expenditure expenditure = new Expenditure();
        expenditure.setProject(project);
        expenditure.setContractor(contractor);
        expenditure.setVoucherNumber(request.getVoucherNumber());
        expenditure.setAmount(request.getAmount());
        expenditure.setPaymentDate(request.getPaymentDate());
        expenditure.setPaymentMode(request.getPaymentMode());
        expenditure.setPaymentReference(request.getPaymentReference());
        expenditure.setDescriptionEn(request.getDescriptionEn());
        expenditure.setDescriptionHi(request.getDescriptionHi());
        expenditure.setFinancialYear(request.getFinancialYear());
        expenditure.setRecordedBy(getCurrentUser());

        Expenditure saved = expenditureRepository.save(expenditure);

        // Update project total_spent
        project.setTotalSpent(projectedTotal);
        projectRepository.save(project);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenditureResponse getExpenditureById(Long id) {
        Expenditure expenditure = expenditureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure", "id", id));
        return toResponse(expenditure);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExpenditureResponse> getExpendituresByProject(Long projectId, Pageable pageable) {
        return expenditureRepository.findByProjectId(projectId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenditureResponse> getExpendituresByProjectAndDateRange(
            Long projectId, LocalDate from, LocalDate to) {
        return expenditureRepository.findByProjectIdAndDateRange(projectId, from, to)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.VERIFY, tableName = "expenditures",
               description = "Expenditure verified by auditor")
    public ExpenditureResponse verifyExpenditure(Long id) {
        Expenditure expenditure = expenditureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure", "id", id));

        if (Boolean.TRUE.equals(expenditure.getIsVerified())) {
            throw new BusinessException("ALREADY_VERIFIED",
                    "This expenditure has already been verified");
        }

        expenditure.setIsVerified(true);
        expenditure.setVerifiedBy(getCurrentUser());
        expenditure.setVerifiedAt(LocalDateTime.now());

        return toResponse(expenditureRepository.save(expenditure));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenditureResponse> getPendingVerifications() {
        return expenditureRepository.findByIsVerified(false)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Mapper ──

    private ExpenditureResponse toResponse(Expenditure e) {
        ExpenditureResponse r = new ExpenditureResponse();
        r.setId(e.getId());
        r.setVoucherNumber(e.getVoucherNumber());
        r.setAmount(e.getAmount());
        r.setPaymentDate(e.getPaymentDate());
        r.setPaymentMode(e.getPaymentMode());
        r.setPaymentReference(e.getPaymentReference());
        r.setDescriptionEn(e.getDescriptionEn());
        r.setDescriptionHi(e.getDescriptionHi());
        r.setFinancialYear(e.getFinancialYear());
        r.setIsVerified(e.getIsVerified());
        r.setVerifiedAt(e.getVerifiedAt());
        r.setCreatedAt(e.getCreatedAt());

        if (e.getProject() != null) {
            r.setProjectId(e.getProject().getId());
            r.setProjectNameEn(e.getProject().getNameEn());
            r.setProjectNameHi(e.getProject().getNameHi());
        }
        if (e.getContractor() != null) {
            r.setContractorId(e.getContractor().getId());
            r.setContractorNameEn(e.getContractor().getNameEn());
        }
        if (e.getVerifiedBy() != null) {
            r.setVerifiedByUsername(e.getVerifiedBy().getUsername());
        }
        if (e.getRecordedBy() != null) {
            r.setRecordedByUsername(e.getRecordedBy().getUsername());
        }
        return r;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl details = (UserDetailsImpl) auth.getPrincipal();
            return userRepository.findById(details.getId()).orElse(null);
        }
        return null;
    }
}
