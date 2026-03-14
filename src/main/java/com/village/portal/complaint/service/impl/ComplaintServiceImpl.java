package com.village.portal.complaint.service.impl;

import com.village.portal.complaint.dto.request.*;
import com.village.portal.complaint.dto.response.*;
import com.village.portal.complaint.entity.*;
import com.village.portal.complaint.enums.*;
import com.village.portal.complaint.repository.*;
import com.village.portal.complaint.service.ComplaintNotificationService;
import com.village.portal.complaint.service.ComplaintService;
import com.village.portal.complaint.service.EscalationService;
import com.village.portal.constants.AppConstants;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.Role;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.UserRepository;
import com.village.portal.service.AuditLogService;
import com.village.portal.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    private final ComplaintRepository            complaintRepository;
    private final ComplaintCategoryRepository    categoryRepository;
    private final ComplaintTimelineRepository    timelineRepository;
    private final ComplaintEvidenceRepository    evidenceRepository;
    private final ComplaintVoteRepository        voteRepository;
    private final VillageRepository              villageRepository;
    private final ResidentVerificationRepository verificationRepository;
    private final UserRepository                 userRepository;
    private final FileStorageService             fileStorageService;
    private final AuditLogService                auditLogService;
    private final ComplaintNotificationService   notificationService;
    private final EscalationService              escalationService;

    public ComplaintServiceImpl(
            ComplaintRepository complaintRepository,
            ComplaintCategoryRepository categoryRepository,
            ComplaintTimelineRepository timelineRepository,
            ComplaintEvidenceRepository evidenceRepository,
            ComplaintVoteRepository voteRepository,
            VillageRepository villageRepository,
            ResidentVerificationRepository verificationRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            AuditLogService auditLogService,
            ComplaintNotificationService notificationService,
            EscalationService escalationService) {
        this.complaintRepository  = complaintRepository;
        this.categoryRepository   = categoryRepository;
        this.timelineRepository   = timelineRepository;
        this.evidenceRepository   = evidenceRepository;
        this.voteRepository       = voteRepository;
        this.villageRepository    = villageRepository;
        this.verificationRepository = verificationRepository;
        this.userRepository       = userRepository;
        this.fileStorageService   = fileStorageService;
        this.auditLogService      = auditLogService;
        this.notificationService  = notificationService;
        this.escalationService    = escalationService;
    }

    // ═══════════════════════════════════════════════════════════
    // RESIDENT OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ComplaintDetailResponse submit(SubmitComplaintRequest request, Long submitterId) {

        User submitter = requireUser(submitterId);

        // Gate 1: phone must be OTP-verified
        boolean phoneVerified = verificationRepository.findByUserId(submitterId)
                .map(ResidentVerification::getIsVerified).orElse(false);
        if (!phoneVerified) {
            throw new BusinessException("PHONE_NOT_VERIFIED",
                    "Please verify your phone number before submitting a complaint.");
        }

        // Gate 2: rate limit — max 3 complaints per 24 hours
        long recentCount = complaintRepository.countRecentBySubmitter(
                submitterId, LocalDateTime.now().minusHours(24), ComplaintStatus.DRAFT);
        if (recentCount >= AppConstants.COMPLAINT_RATE_LIMIT_PER_DAY) {
            throw new BusinessException("RATE_LIMIT_EXCEEDED",
                    "You have reached the maximum of " + AppConstants.COMPLAINT_RATE_LIMIT_PER_DAY
                            + " complaints per day.");
        }

        ComplaintCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found","",""));

        Village village = villageRepository.findById(AppConstants.DEFAULT_VILLAGE_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Village not found","",""));

        // Gate 3: duplicate detection
        List<Complaint> potentials = complaintRepository.findPotentialDuplicates(
                submitterId, request.getCategoryId(), request.getWardNumber(),
                List.of(ComplaintStatus.REJECTED, ComplaintStatus.CLOSED),
                LocalDateTime.now().minusDays(AppConstants.DUPLICATE_DETECTION_WINDOW_DAYS));

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(generateComplaintNumber());
        complaint.setVillage(village);
        complaint.setWardNumber(request.getWardNumber());
        complaint.setCategory(category);
        complaint.setTitleEn(sanitize(request.getTitleEn()));
        complaint.setTitleHi(request.getTitleHi());
        complaint.setDescriptionEn(sanitize(request.getDescriptionEn()));
        complaint.setDescriptionHi(request.getDescriptionHi());
        complaint.setLocationText(request.getLocationText());
        complaint.setLatitude(request.getLatitude());
        complaint.setLongitude(request.getLongitude());
        complaint.setSubmitter(submitter);
        complaint.setSubmitterPhoneVerified(true);
        complaint.setIsAnonymousDisplay(Boolean.TRUE.equals(request.getIsAnonymousDisplay()));
        complaint.setPriority(request.getPriority() != null
                ? request.getPriority() : category.getDefaultPriority());

        // Flag potential duplicates — admin will review
        if (!potentials.isEmpty()) {
            complaint.setIsDuplicateFlagged(true);
            complaint.setDuplicateOf(potentials.get(0));
            complaint.setStatus(ComplaintStatus.SUBMITTED); // goes to admin queue
        } else {
            complaint.setStatus(ComplaintStatus.SUBMITTED);
        }

        complaintRepository.save(complaint);

        // First timeline entry
        appendTimeline(complaint, null, complaint.getStatus(),
                submitter.getId(), "CITIZEN", submitter.getFullName(),
                "Complaint submitted by resident", true);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaint.getId(),
                AuditAction.CREATE, null, null,
                "Complaint submitted: " + complaint.getComplaintNumber());

        // Notify submitter
        notificationService.send(submitterId, complaint,
                NotificationType.COMPLAINT_SUBMITTED, null, null);

        return buildDetail(complaint, submitterId, false);
    }

    @Override
    @Transactional
    public void uploadEvidence(Long complaintId, List<MultipartFile> files,
                                Long uploaderId, boolean isResolutionProof) {

        Complaint complaint = requireComplaint(complaintId);
        User uploader = requireUser(uploaderId);

        // Max 5 files total per complaint
        long existingCount = evidenceRepository.countByComplaintIdAndIsDeletedFalse(complaintId);
        if (existingCount + files.size() > AppConstants.COMPLAINT_MAX_EVIDENCE_FILES) {
            throw new BusinessException("MAX_EVIDENCE_EXCEEDED",
                    "Maximum " + AppConstants.COMPLAINT_MAX_EVIDENCE_FILES + " evidence files allowed per complaint.");
        }

        EvidenceType type = isResolutionProof ? EvidenceType.RESOLUTION_PROOF : EvidenceType.SUBMISSION_PHOTO;
        boolean isPublic  = true; // all evidence is public (private internal notes handled separately)

        String subFolder = buildEvidenceFolder(complaint, type);

        for (MultipartFile file : files) {
            String storedPath = fileStorageService.storeFile(file, subFolder);

            ComplaintEvidence evidence = new ComplaintEvidence();
            evidence.setComplaint(complaint);
            evidence.setEvidenceType(type);
            evidence.setFileName(storedPath.contains("/")
                    ? storedPath.substring(storedPath.lastIndexOf('/') + 1) : storedPath);
            evidence.setOriginalFileName(file.getOriginalFilename());
            evidence.setFileType(file.getContentType());
            evidence.setFileSizeKb((int)(file.getSize() / 1024));
            evidence.setFilePath(storedPath);
            evidence.setUploadedBy(uploader);
            evidence.setIsPublic(isPublic);
            evidenceRepository.save(evidence);
        }

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.FILE_UPLOAD, null, null,
                files.size() + " file(s) uploaded to complaint " + complaint.getComplaintNumber());
    }

    @Override
    @Transactional
    public VoteResponse toggleVote(Long complaintId, Long userId) {
        Complaint complaint = requireComplaint(complaintId);
        if (complaint.getStatus() == ComplaintStatus.CLOSED
                || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new BusinessException("VOTE_NOT_ALLOWED", "Cannot vote on a closed or rejected complaint.");
        }

        Optional<ComplaintVote> existing = voteRepository.findByComplaintIdAndUserId(complaintId, userId);
        boolean voted;
        if (existing.isPresent()) {
            voteRepository.delete(existing.get());
            complaint.setSupportCount(Math.max(0, complaint.getSupportCount() - 1));
            voted = false;
        } else {
            User user = requireUser(userId);
            ComplaintVote vote = new ComplaintVote();
            vote.setComplaint(complaint);
            vote.setUser(user);
            voteRepository.save(vote);
            complaint.setSupportCount(complaint.getSupportCount() + 1);
            voted = true;

            // Check if support threshold triggers escalation
            if (complaint.getSupportCount() >= AppConstants.SUPPORT_THRESHOLD_FOR_ESCALATION
                    && complaint.getEscalationLevel() == 0) {
                escalationService.escalate(complaint, EscalationTrigger.SUPPORT_THRESHOLD,
                        "Community support threshold reached", null);
            }
        }
        complaintRepository.save(complaint);
        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.VOTE, null, null,
                (voted ? "Vote added" : "Vote removed") + " by user " + userId);
        return new VoteResponse(voted, complaint.getSupportCount());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintSummaryResponse> getMyComplaints(Long submitterId, Pageable pageable) {
        return complaintRepository.findBySubmitterIdOrderByCreatedAtDesc(submitterId, pageable)
                .map(c -> buildSummary(c, submitterId));
    }

    @Override
    @Transactional
    public ComplaintDetailResponse acceptResolution(Long complaintId, Long citizenId) {
        Complaint complaint = requireComplaint(complaintId);
        assertOwner(complaint, citizenId);
        assertStatus(complaint, ComplaintStatus.RESOLVED, "accept resolution");

        transition(complaint, ComplaintStatus.CLOSED,
                citizenId, "CITIZEN", requireUser(citizenId).getFullName(),
                "Citizen accepted the resolution.", true);

        complaint.setClosedAt(LocalDateTime.now());
        complaintRepository.save(complaint);

        // Notify officer and admin
        if (complaint.getAssignedOfficer() != null) {
            notificationService.send(complaint.getAssignedOfficer().getId(), complaint,
                    NotificationType.RESOLUTION_ACCEPTED, null, null);
        }
        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.UPDATE, null, null,
                "Resolution accepted: " + complaint.getComplaintNumber());
        return buildDetail(complaint, citizenId, false);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse rejectResolution(Long complaintId, Long citizenId,
                                                     ResolutionResponseRequest request) {
        Complaint complaint = requireComplaint(complaintId);
        assertOwner(complaint, citizenId);
        assertStatus(complaint, ComplaintStatus.RESOLVED, "reject resolution");

        if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
            throw new BusinessException("REASON_REQUIRED",
                    "Please provide a reason for rejecting the resolution.");
        }

        transition(complaint, ComplaintStatus.IN_PROGRESS,
                citizenId, "CITIZEN", requireUser(citizenId).getFullName(),
                "Citizen rejected resolution: " + request.getRejectionReason(), true);

        // Auto-escalate on citizen rejection
        escalationService.escalate(complaint, EscalationTrigger.CITIZEN_REJECTION,
                "Citizen rejected resolution: " + request.getRejectionReason(), citizenId);

        complaintRepository.save(complaint);

        if (complaint.getAssignedOfficer() != null) {
            notificationService.send(complaint.getAssignedOfficer().getId(), complaint,
                    NotificationType.RESOLUTION_REJECTED,
                    request.getRejectionReason(), null);
        }
        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.REJECT, null, null,
                "Resolution rejected: " + complaint.getComplaintNumber());
        return buildDetail(complaint, citizenId, false);
    }

    // ═══════════════════════════════════════════════════════════
    // OFFICER OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintSummaryResponse> getAssignedComplaints(Long officerId,
                                                                  List<ComplaintStatus> statuses,
                                                                  Pageable pageable) {
        List<ComplaintStatus> filter = (statuses == null || statuses.isEmpty())
                ? List.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED)
                : statuses;
        return complaintRepository.findByAssignedOfficerIdAndStatusIn(officerId, filter, pageable)
                .map(c -> buildSummary(c, officerId));
    }

    @Override
    @Transactional
    public ComplaintDetailResponse postUpdate(Long complaintId, PostUpdateRequest request, Long officerId) {
        Complaint complaint = requireComplaint(complaintId);
        assertAssignedOfficer(complaint, officerId);

        // Move to IN_PROGRESS on first update if still ASSIGNED
        if (complaint.getStatus() == ComplaintStatus.ASSIGNED) {
            complaint.setStatus(ComplaintStatus.IN_PROGRESS);
            complaintRepository.save(complaint);
        }

        appendTimeline(complaint, complaint.getStatus(), complaint.getStatus(),
                officerId, "OFFICER", requireUser(officerId).getFullName(),
                request.getNote(), Boolean.TRUE.equals(request.getIsPublicNote()));

        // Notify submitter if public note
        if (Boolean.TRUE.equals(request.getIsPublicNote())) {
            notificationService.send(complaint.getSubmitter().getId(), complaint,
                    NotificationType.OFFICER_UPDATE, request.getNote(), null);
        }
        return buildDetail(complaint, officerId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse resolve(Long complaintId, ResolveComplaintRequest request, Long officerId) {
        Complaint complaint = requireComplaint(complaintId);
        assertAssignedOfficer(complaint, officerId);

        if (complaint.getStatus() != ComplaintStatus.ASSIGNED
                && complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new BusinessException("INVALID_STATUS",
                    "Complaint must be ASSIGNED or IN_PROGRESS to resolve.");
        }

        // Resolution must have at least one proof file
        long proofCount = evidenceRepository.countByComplaintIdAndIsDeletedFalse(complaintId);
        if (proofCount == 0) {
            throw new BusinessException("PROOF_REQUIRED",
                    "Please upload at least one resolution proof before marking as resolved.");
        }

        transition(complaint, ComplaintStatus.RESOLVED,
                officerId, "OFFICER", requireUser(officerId).getFullName(),
                request.getResolutionNote(), true);

        complaint.setResolutionNote(request.getResolutionNote());
        complaint.setResolvedAt(LocalDateTime.now());
        complaint.setCitizenResponseDeadline(
                LocalDateTime.now().plusDays(AppConstants.CITIZEN_RESOLUTION_RESPONSE_DAYS));
        complaintRepository.save(complaint);

        notificationService.send(complaint.getSubmitter().getId(), complaint,
                NotificationType.RESOLUTION_READY, request.getResolutionNote(), null);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.RESOLVE, null, null,
                "Resolved: " + complaint.getComplaintNumber() + " by officer " + officerId);
        return buildDetail(complaint, officerId, true);
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional
    public ComplaintDetailResponse verify(Long complaintId, String note, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        assertStatus(complaint, ComplaintStatus.SUBMITTED, "verify");

        transition(complaint, ComplaintStatus.VERIFIED,
                adminId, "ADMIN", requireUser(adminId).getFullName(),
                note != null ? note : "Complaint verified by admin.", true);
        complaintRepository.save(complaint);

        notificationService.send(complaint.getSubmitter().getId(), complaint,
                NotificationType.COMPLAINT_VERIFIED, null, null);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.VERIFY, null, null,
                "Verified: " + complaint.getComplaintNumber());
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse reject(Long complaintId, RejectComplaintRequest request, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        if (complaint.getStatus() == ComplaintStatus.CLOSED) {
            throw new BusinessException("INVALID_STATUS", "Cannot reject a closed complaint.");
        }

        transition(complaint, ComplaintStatus.REJECTED,
                adminId, "ADMIN", requireUser(adminId).getFullName(),
                request.getReason(), true);
        complaint.setRejectionReason(request.getReason());
        complaintRepository.save(complaint);

        notificationService.send(complaint.getSubmitter().getId(), complaint,
                NotificationType.COMPLAINT_REJECTED, request.getReason(), null);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.REJECT, null, null,
                "Rejected: " + complaint.getComplaintNumber() + " — " + request.getReason());
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse assign(Long complaintId, AssignComplaintRequest request, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        if (complaint.getStatus() != ComplaintStatus.VERIFIED) {
            throw new BusinessException("INVALID_STATUS", "Only VERIFIED complaints can be assigned.");
        }

        User officer = userRepository.findById(request.getOfficerId())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found","",""));
        if (officer.getRole() != Role.OFFICER && officer.getRole() != Role.ADMIN) {
            throw new BusinessException("INVALID_ROLE", "Selected user is not an officer.");
        }

        // Calculate due date from category SLA if not provided
        LocalDate dueDate = request.getDueDate() != null
                ? request.getDueDate()
                : LocalDate.now().plusDays(complaint.getCategory().getSlaDays());

        complaint.setAssignedOfficer(officer);
        complaint.setAssignedAt(LocalDateTime.now());
        complaint.setDueDate(dueDate);
        transition(complaint, ComplaintStatus.ASSIGNED,
                adminId, "ADMIN", requireUser(adminId).getFullName(),
                "Assigned to " + officer.getFullName() + ". Due: " + dueDate, true);
        complaintRepository.save(complaint);

        notificationService.send(officer.getId(), complaint,
                NotificationType.COMPLAINT_ASSIGNED, null, null);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.ASSIGN, null, null,
                "Assigned " + complaint.getComplaintNumber() + " to officer " + officer.getUsername());
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse escalate(Long complaintId, int level, String note, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        // Delegate fully to EscalationService — it handles level update, log, and notifications
        escalationService.escalate(complaint, EscalationTrigger.ADMIN_OVERRIDE, note, adminId);

        auditLogService.log(AppConstants.TABLE_COMPLAINTS, complaintId,
                AuditAction.ESCALATE, null, null,
                "Manual escalation to level " + level + ": " + complaint.getComplaintNumber());
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse changePriority(Long complaintId, ComplaintPriority priority, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        complaint.setPriority(priority);
        complaintRepository.save(complaint);
        appendTimeline(complaint, complaint.getStatus(), complaint.getStatus(),
                adminId, "ADMIN", requireUser(adminId).getFullName(),
                "Priority changed to " + priority, false);
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional
    public ComplaintDetailResponse toggleVisibility(Long complaintId, Long adminId) {
        Complaint complaint = requireComplaint(complaintId);
        complaint.setIsPublic(!complaint.getIsPublic());
        complaintRepository.save(complaint);
        return buildDetail(complaint, adminId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintSummaryResponse> getAllForAdmin(ComplaintStatus status,
                                                          ComplaintPriority priority,
                                                          Long categoryId,
                                                          boolean escalatedOnly,
                                                          Pageable pageable) {
        return complaintRepository.findAllForAdmin(status, priority, categoryId, escalatedOnly, pageable)
                .map(c -> buildSummary(c, null));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintStatsResponse getStats(Long villageId) {
        List<Object[]> rows = complaintRepository.countByStatusForVillage(
                villageId != null ? villageId : AppConstants.DEFAULT_VILLAGE_ID);

        ComplaintStatsResponse stats = new ComplaintStatsResponse();
        long total = 0;
        for (Object[] row : rows) {
            ComplaintStatus st = (ComplaintStatus) row[0];
            long count = (Long) row[1];
            total += count;
            switch (st) {
                case SUBMITTED, VERIFIED -> stats.setSubmitted(stats.getSubmitted() + count);
                case IN_PROGRESS         -> stats.setInProgress(count);
                case RESOLVED            -> stats.setResolved(count);
                case CLOSED              -> stats.setClosed(count);
                case REJECTED            -> stats.setRejected(count);
                default -> {}
            }
        }
        stats.setTotal(total);
        return stats;
    }

    // ═══════════════════════════════════════════════════════════
    // PUBLIC OPERATIONS
    // ═══════════════════════════════════════════════════════════

    @Override
    @Transactional(readOnly = true)
    public Page<ComplaintSummaryResponse> getPublicComplaints(Long villageId,
                                                               ComplaintStatus status,
                                                               Long categoryId,
                                                               Integer wardNumber,
                                                               String keyword,
                                                               Pageable pageable) {
        long vid = villageId != null ? villageId : AppConstants.DEFAULT_VILLAGE_ID;
        Page<Complaint> page;
        if (keyword != null && !keyword.isBlank()) {
            page = complaintRepository.searchPublicComplaints(vid, keyword + "*", pageable);
        } else {
            page = complaintRepository.findPublicComplaints(vid, status, categoryId, wardNumber, pageable);
        }
        return page.map(c -> buildSummary(c, null));
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintDetailResponse getPublicDetail(String complaintNumber, Long viewerUserId) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Complaint not found: " + complaintNumber,"",""));

        boolean isStaff = viewerUserId != null && userRepository.findById(viewerUserId)
                .map(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.OFFICER
                        || u.getRole() == Role.AUDITOR)
                .orElse(false);

        if (!complaint.getIsPublic() && !isStaff) {
            throw new BusinessException("ACCESS_DENIED", "This complaint is not publicly available.");
        }
        return buildDetail(complaint, viewerUserId, isStaff);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc()
                .stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private void transition(Complaint complaint, ComplaintStatus toStatus,
                             Long actorId, String actorRole, String actorName,
                             String note, boolean isPublic) {
        ComplaintStatus from = complaint.getStatus();
        complaint.setStatus(toStatus);
        appendTimeline(complaint, from, toStatus, actorId, actorRole, actorName, note, isPublic);
    }

    private void appendTimeline(Complaint complaint,
                                  ComplaintStatus from, ComplaintStatus to,
                                  Long actorId, String actorRole, String actorName,
                                  String note, boolean isPublic) {
        ComplaintTimeline entry = new ComplaintTimeline();
        entry.setComplaint(complaint);
        entry.setFromStatus(from);
        entry.setToStatus(to);
        entry.setActorUserId(actorId);
        entry.setActorRole(actorRole);
        entry.setActorName(actorName);
        entry.setNote(note);
        entry.setIsPublicNote(isPublic);
        timelineRepository.save(entry);
    }

    private ComplaintDetailResponse buildDetail(Complaint c, Long viewerUserId, boolean includePrivate) {
        ComplaintDetailResponse r = new ComplaintDetailResponse();
        r.setId(c.getId());
        r.setComplaintNumber(c.getComplaintNumber());
        r.setTitleEn(c.getTitleEn());
        r.setTitleHi(c.getTitleHi());
        r.setDescriptionEn(c.getDescriptionEn());
        r.setDescriptionHi(c.getDescriptionHi());
        r.setCategoryNameEn(c.getCategory().getNameEn());
        r.setCategoryNameHi(c.getCategory().getNameHi());
        r.setWardNumber(c.getWardNumber());
        r.setLocationText(c.getLocationText());
        r.setLatitude(c.getLatitude());
        r.setLongitude(c.getLongitude());
        r.setStatus(c.getStatus());
        r.setPriority(c.getPriority());
        r.setEscalationLevel(c.getEscalationLevel());
        r.setSupportCount(c.getSupportCount());
        r.setSubmitterDisplayName(displayName(c));
        r.setAssignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getFullName() : null);
        r.setDueDate(c.getDueDate());
        r.setIsOverdue(c.getDueDate() != null && c.getDueDate().isBefore(LocalDate.now())
                && c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED);
        r.setResolutionNote(c.getResolutionNote());
        r.setResolvedAt(c.getResolvedAt());
        r.setClosedAt(c.getClosedAt());
        r.setCitizenResponseDeadline(c.getCitizenResponseDeadline());
        r.setRejectionReason(c.getRejectionReason());
        r.setIsDuplicateFlagged(c.getIsDuplicateFlagged());
        if (c.getDuplicateOf() != null) r.setDuplicateOfNumber(c.getDuplicateOf().getComplaintNumber());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());

        // Vote state for authenticated viewer
        if (viewerUserId != null) {
            r.setHasVoted(voteRepository.existsByComplaintIdAndUserId(c.getId(), viewerUserId));
        }

        // Timeline
        List<ComplaintTimeline> timeline = includePrivate
                ? timelineRepository.findByComplaintIdOrderByCreatedAtAsc(c.getId())
                : timelineRepository.findByComplaintIdAndIsPublicNoteTrueOrderByCreatedAtAsc(c.getId());
        r.setTimeline(timeline.stream().map(this::toTimelineResponse).collect(Collectors.toList()));

        // Evidence
        List<ComplaintEvidence> evidence = includePrivate
                ? evidenceRepository.findByComplaintIdAndIsDeletedFalseOrderByCreatedAtAsc(c.getId())
                : evidenceRepository.findByComplaintIdAndIsPublicTrueAndIsDeletedFalseOrderByCreatedAtAsc(c.getId());
        r.setEvidence(evidence.stream().map(e -> toEvidenceResponse(e, c.getId())).collect(Collectors.toList()));

        return r;
    }

    private ComplaintSummaryResponse buildSummary(Complaint c, Long viewerUserId) {
        ComplaintSummaryResponse r = new ComplaintSummaryResponse();
        r.setId(c.getId());
        r.setComplaintNumber(c.getComplaintNumber());
        r.setTitleEn(c.getTitleEn());
        r.setTitleHi(c.getTitleHi());
        r.setCategoryNameEn(c.getCategory().getNameEn());
        r.setCategoryNameHi(c.getCategory().getNameHi());
        r.setWardNumber(c.getWardNumber());
        r.setLocationText(c.getLocationText());
        r.setStatus(c.getStatus());
        r.setPriority(c.getPriority());
        r.setEscalationLevel(c.getEscalationLevel());
        r.setSupportCount(c.getSupportCount());
        r.setSubmitterDisplayName(displayName(c));
        r.setAssignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getFullName() : null);
        r.setDueDate(c.getDueDate());
        r.setIsOverdue(c.getDueDate() != null && c.getDueDate().isBefore(LocalDate.now())
                && c.getStatus() != ComplaintStatus.RESOLVED && c.getStatus() != ComplaintStatus.CLOSED);
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }

    private TimelineEntryResponse toTimelineResponse(ComplaintTimeline t) {
        TimelineEntryResponse r = new TimelineEntryResponse();
        r.setId(t.getId());
        r.setFromStatus(t.getFromStatus());
        r.setToStatus(t.getToStatus());
        r.setActorRole(t.getActorRole());
        r.setActorName(t.getActorName());
        r.setNote(t.getNote());
        r.setIsPublicNote(t.getIsPublicNote());
        r.setCreatedAt(t.getCreatedAt());
        return r;
    }

    private EvidenceResponse toEvidenceResponse(ComplaintEvidence e, Long complaintId) {
        EvidenceResponse r = new EvidenceResponse();
        r.setId(e.getId());
        r.setEvidenceType(e.getEvidenceType());
        r.setOriginalFileName(e.getOriginalFileName());
        r.setFileType(e.getFileType());
        r.setFileSizeKb(e.getFileSizeKb());
        r.setDownloadUrl("/api/complaints/" + complaintId + "/evidence/" + e.getId() + "/download");
        r.setIsPublic(e.getIsPublic());
        r.setUploadedByName(e.getUploadedBy() != null ? e.getUploadedBy().getFullName() : null);
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    private CategoryResponse toCategoryResponse(ComplaintCategory cat) {
        CategoryResponse r = new CategoryResponse();
        r.setId(cat.getId());
        r.setNameEn(cat.getNameEn());
        r.setNameHi(cat.getNameHi());
        r.setDescriptionEn(cat.getDescriptionEn());
        r.setSlaDays(cat.getSlaDays());
        r.setEscalationAfterDays(cat.getEscalationAfterDays());
        r.setDefaultPriority(cat.getDefaultPriority());
        r.setDisplayOrder(cat.getDisplayOrder());
        return r;
    }

    private String displayName(Complaint c) {
        if (Boolean.TRUE.equals(c.getIsAnonymousDisplay())) return "Anonymous Resident";
        return c.getSubmitter() != null ? c.getSubmitter().getFullName() : "Resident";
    }

    private String buildEvidenceFolder(Complaint c, EvidenceType type) {
        String typeFolder = type == EvidenceType.RESOLUTION_PROOF ? "resolution" : "submission";
        return "complaints/" + c.getVillage().getId()
                + "/" + c.getCreatedAt().getYear()
                + "/" + String.format("%02d", c.getCreatedAt().getMonthValue())
                + "/" + c.getId()
                + "/" + typeFolder;
    }

    private String generateComplaintNumber() {
        int year = LocalDate.now().getYear();
        long count = complaintRepository.count() + 1;
        return String.format("CMP-%d-%06d", year, count);
    }

    private String sanitize(String input) {
        if (input == null) return null;
        // Strip basic XSS vectors — remove script/iframe tags
        return input.replaceAll("(?i)<script.*?>.*?</script>", "")
                    .replaceAll("(?i)<iframe.*?>.*?</iframe>", "")
                    .trim();
    }

    // ── Guard helpers ─────────────────────────────────────────

    private Complaint requireComplaint(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found: " + id,"",""));
    }

    private User requireUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id,"",""));
    }

    private void assertStatus(Complaint c, ComplaintStatus expected, String action) {
        if (c.getStatus() != expected) {
            throw new BusinessException("INVALID_STATUS",
                    "Cannot " + action + ". Complaint is " + c.getStatus() + ", expected " + expected);
        }
    }

    private void assertOwner(Complaint c, Long userId) {
        if (!c.getSubmitter().getId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED", "You can only act on your own complaints.");
        }
    }

    private void assertAssignedOfficer(Complaint c, Long officerId) {
        if (c.getAssignedOfficer() == null || !c.getAssignedOfficer().getId().equals(officerId)) {
            throw new BusinessException("ACCESS_DENIED", "This complaint is not assigned to you.");
        }
    }
}
