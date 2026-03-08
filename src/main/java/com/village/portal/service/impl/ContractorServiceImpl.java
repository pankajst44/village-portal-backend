package com.village.portal.service.impl;

import com.village.portal.aspect.Auditable;
import com.village.portal.dto.request.CreateContractorRequest;
import com.village.portal.dto.response.ContractorResponse;
import com.village.portal.entity.Contractor;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.ContractorCategory;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.DuplicateResourceException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.ContractorRepository;
import com.village.portal.repository.ExpenditureRepository;
import com.village.portal.repository.ProjectContractorRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.ContractorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractorServiceImpl implements ContractorService {

    private final ContractorRepository        contractorRepository;
    private final ExpenditureRepository       expenditureRepository;
    private final ProjectContractorRepository projectContractorRepository;
    private final UserRepository              userRepository;

    public ContractorServiceImpl(ContractorRepository contractorRepository,
                                 ExpenditureRepository expenditureRepository,
                                 ProjectContractorRepository projectContractorRepository,
                                 UserRepository userRepository) {
        this.contractorRepository        = contractorRepository;
        this.expenditureRepository       = expenditureRepository;
        this.projectContractorRepository = projectContractorRepository;
        this.userRepository              = userRepository;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, tableName = "contractors",
            description = "New contractor registered")
    public ContractorResponse createContractor(CreateContractorRequest request) {

        if (contractorRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException(
                    "Contractor with registration number '"
                            + request.getRegistrationNumber() + "' already exists");
        }

        if (request.getPanNumber() != null && !request.getPanNumber().isBlank()
                && contractorRepository.existsByPanNumber(request.getPanNumber())) {
            throw new DuplicateResourceException(
                    "Contractor with PAN number '" + request.getPanNumber() + "' already exists");
        }

        Contractor contractor = new Contractor();
        mapRequestToEntity(request, contractor);
        contractor.setIsBlacklisted(false);
        contractor.setCreatedBy(getCurrentUser());

        return toResponse(contractorRepository.save(contractor));
    }

    // ── READ ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ContractorResponse getContractorById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractorResponse> getAllContractors(Pageable pageable) {
        return contractorRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContractorResponse> getActiveContractors(Pageable pageable) {
        return contractorRepository.findByIsBlacklistedFalse(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractorResponse> getContractorsByCategory(ContractorCategory category) {
        return contractorRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContractorResponse> getBlacklistedContractors() {
        return contractorRepository.findByIsBlacklisted(true)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "contractors",
            description = "Contractor details updated")
    public ContractorResponse updateContractor(Long id, CreateContractorRequest request) {

        Contractor contractor = findOrThrow(id);

        // Registration number uniqueness — skip check if unchanged
        if (!contractor.getRegistrationNumber().equals(request.getRegistrationNumber())
                && contractorRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException(
                    "Registration number '" + request.getRegistrationNumber() + "' is already in use");
        }

        // PAN number uniqueness — skip check if unchanged or blank
        String incomingPan = request.getPanNumber();
        if (incomingPan != null && !incomingPan.isBlank()
                && !incomingPan.equals(contractor.getPanNumber())
                && contractorRepository.existsByPanNumber(incomingPan)) {
            throw new DuplicateResourceException(
                    "PAN number '" + incomingPan + "' is already registered to another contractor");
        }

        mapRequestToEntity(request, contractor);

        return toResponse(contractorRepository.save(contractor));
    }

    // ── BLACKLIST ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "contractors",
            description = "Contractor blacklisted")
    public ContractorResponse blacklistContractor(Long id, String reason) {

        Contractor contractor = findOrThrow(id);

        if (Boolean.TRUE.equals(contractor.getIsBlacklisted())) {
            throw new BusinessException("ALREADY_BLACKLISTED",
                    "Contractor is already blacklisted");
        }

        if (reason == null || reason.isBlank()) {
            throw new BusinessException("REASON_REQUIRED",
                    "A reason must be provided when blacklisting a contractor");
        }

        contractor.setIsBlacklisted(true);
        contractor.setBlacklistReason(reason.trim());

        return toResponse(contractorRepository.save(contractor));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "contractors",
            description = "Contractor removed from blacklist")
    public ContractorResponse removeFromBlacklist(Long id) {

        Contractor contractor = findOrThrow(id);

        if (Boolean.FALSE.equals(contractor.getIsBlacklisted())) {
            throw new BusinessException("NOT_BLACKLISTED",
                    "Contractor is not currently blacklisted");
        }

        contractor.setIsBlacklisted(false);
        contractor.setBlacklistReason(null);

        return toResponse(contractorRepository.save(contractor));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, tableName = "contractors",
            description = "Contractor deleted")
    public void deleteContractor(Long id) {

        Contractor contractor = findOrThrow(id);

        // Cannot delete if the contractor has recorded expenditures
        boolean hasExpenditures = !expenditureRepository.findByContractorId(id).isEmpty();
        if (hasExpenditures) {
            throw new BusinessException("HAS_EXPENDITURES",
                    "Cannot delete a contractor that has recorded expenditures");
        }

        // Cannot delete if the contractor is assigned to any project
        boolean hasAssignments = !projectContractorRepository.findByContractorId(id).isEmpty();
        if (hasAssignments) {
            throw new BusinessException("HAS_PROJECT_ASSIGNMENTS",
                    "Cannot delete a contractor that is assigned to one or more projects");
        }

        contractorRepository.delete(contractor);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Contractor findOrThrow(Long id) {
        return contractorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contractor", "id", id));
    }

    /** Copies all editable request fields onto the entity. Used by create and update. */
    private void mapRequestToEntity(CreateContractorRequest req, Contractor contractor) {
        contractor.setNameEn(req.getNameEn());
        contractor.setNameHi(req.getNameHi());
        contractor.setRegistrationNumber(req.getRegistrationNumber());
        contractor.setPanNumber(req.getPanNumber());
        contractor.setGstNumber(req.getGstNumber());
        contractor.setContactPerson(req.getContactPerson());
        contractor.setPhone(req.getPhone());
        contractor.setEmail(req.getEmail());
        contractor.setAddressEn(req.getAddressEn());
        contractor.setAddressHi(req.getAddressHi());
        contractor.setCategory(req.getCategory() != null
                ? req.getCategory() : ContractorCategory.OTHER);
    }

    /** Maps a Contractor entity to its response DTO. */
    private ContractorResponse toResponse(Contractor c) {
        ContractorResponse r = new ContractorResponse();
        r.setId(c.getId());
        r.setNameEn(c.getNameEn());
        r.setNameHi(c.getNameHi());
        r.setRegistrationNumber(c.getRegistrationNumber());
        r.setPanNumber(c.getPanNumber());
        r.setGstNumber(c.getGstNumber());
        r.setContactPerson(c.getContactPerson());
        r.setPhone(c.getPhone());
        r.setEmail(c.getEmail());
        r.setAddressEn(c.getAddressEn());
        r.setAddressHi(c.getAddressHi());
        r.setCategory(c.getCategory());
        r.setIsBlacklisted(c.getIsBlacklisted());
        r.setBlacklistReason(c.getBlacklistReason());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());

        if (c.getCreatedBy() != null) {
            r.setCreatedByUsername(c.getCreatedBy().getUsername());
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
