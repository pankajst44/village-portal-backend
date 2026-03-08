package com.village.portal.service.impl;

import com.village.portal.aspect.Auditable;
import com.village.portal.dto.request.CreateProjectRequest;
import com.village.portal.dto.request.UpdateProjectProgressRequest;
import com.village.portal.dto.response.ProjectResponse;
import com.village.portal.entity.Fund;
import com.village.portal.entity.Project;
import com.village.portal.entity.User;
import com.village.portal.enums.AuditAction;
import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;
import com.village.portal.exception.BusinessException;
import com.village.portal.exception.DuplicateResourceException;
import com.village.portal.exception.ResourceNotFoundException;
import com.village.portal.repository.FundRepository;
import com.village.portal.repository.ProjectRepository;
import com.village.portal.repository.UserRepository;
import com.village.portal.security.UserDetailsImpl;
import com.village.portal.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final FundRepository fundRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                               FundRepository fundRepository,
                               UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.fundRepository    = fundRepository;
        this.userRepository    = userRepository;
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.CREATE, tableName = "projects",
               description = "New project created")
    public ProjectResponse createProject(CreateProjectRequest request) {

        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateResourceException(
                    "Project with code '" + request.getProjectCode() + "' already exists");
        }

        Project project = new Project();
        project.setProjectCode(request.getProjectCode());
        project.setNameEn(request.getNameEn());
        project.setNameHi(request.getNameHi());
        project.setDescriptionEn(request.getDescriptionEn());
        project.setDescriptionHi(request.getDescriptionHi());
        project.setLocationEn(request.getLocationEn());
        project.setLocationHi(request.getLocationHi());
        project.setProjectType(request.getProjectType());
        project.setStatus(request.getStatus() != null ? request.getStatus() : ProjectStatus.PLANNED);
        project.setAllocatedBudget(request.getAllocatedBudget());
        project.setTotalSpent(BigDecimal.ZERO);
        project.setProgressPercent(request.getProgressPercent() != null ? request.getProgressPercent() : 0);
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        project.setIsPublicVisible(request.getIsPublicVisible() != null ? request.getIsPublicVisible() : true);

        // Link fund if provided
        if (request.getFundId() != null) {
            Fund fund = fundRepository.findById(request.getFundId())
                    .orElseThrow(() -> new ResourceNotFoundException("Fund", "id", request.getFundId()));
            project.setFund(fund);
        }

        // Link assigned officer if provided
        if (request.getAssignedOfficerId() != null) {
            User officer = userRepository.findById(request.getAssignedOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedOfficerId()));
            project.setAssignedOfficer(officer);
        }

        // Set created by from security context
        project.setCreatedBy(getCurrentUser());

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectByCode(String code) {
        Project project = projectRepository.findByProjectCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "code", code));
        return toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getPublicProjects(ProjectStatus status, ProjectType type, Pageable pageable) {
        return projectRepository.findPublicProjectsFiltered(status, type, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByOfficer(Long officerId) {
        return projectRepository.findByAssignedOfficerId(officerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "projects",
               description = "Project details updated")
    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // If code is changing, verify no duplicate
        if (!project.getProjectCode().equals(request.getProjectCode())
                && projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new DuplicateResourceException(
                    "Project code '" + request.getProjectCode() + "' is already in use");
        }

        project.setProjectCode(request.getProjectCode());
        project.setNameEn(request.getNameEn());
        project.setNameHi(request.getNameHi());
        project.setDescriptionEn(request.getDescriptionEn());
        project.setDescriptionHi(request.getDescriptionHi());
        project.setLocationEn(request.getLocationEn());
        project.setLocationHi(request.getLocationHi());
        project.setProjectType(request.getProjectType());
        project.setStatus(request.getStatus());
        project.setAllocatedBudget(request.getAllocatedBudget());
        project.setProgressPercent(request.getProgressPercent());
        project.setStartDate(request.getStartDate());
        project.setExpectedEndDate(request.getExpectedEndDate());
        project.setIsPublicVisible(request.getIsPublicVisible());

        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.UPDATE, tableName = "projects",
               description = "Project progress updated")
    public ProjectResponse updateProgress(Long id, UpdateProjectProgressRequest request) {

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Business rule: cannot set progress to 100 unless status is COMPLETED
        if (request.getProgressPercent() == 100
                && request.getStatus() != null
                && request.getStatus() != ProjectStatus.COMPLETED) {
            throw new BusinessException("INVALID_PROGRESS",
                    "Progress can only be 100% when project status is COMPLETED");
        }

        // Business rule: cannot set progress on a CANCELLED project
        if (project.getStatus() == ProjectStatus.CANCELLED) {
            throw new BusinessException("PROJECT_CANCELLED",
                    "Cannot update progress on a cancelled project");
        }

        project.setProgressPercent(request.getProgressPercent());
        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        return toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    @Auditable(action = AuditAction.DELETE, tableName = "projects",
               description = "Project deleted")
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        // Business rule: cannot delete a project with recorded expenditures
        if (project.getTotalSpent().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("HAS_EXPENDITURES",
                    "Cannot delete a project that has recorded expenditures");
        }

        projectRepository.delete(project);
    }

    // ── Mapper: Entity → Response DTO ──

    private ProjectResponse toResponse(Project p) {
        ProjectResponse r = new ProjectResponse();
        r.setId(p.getId());
        r.setProjectCode(p.getProjectCode());
        r.setNameEn(p.getNameEn());
        r.setNameHi(p.getNameHi());
        r.setDescriptionEn(p.getDescriptionEn());
        r.setDescriptionHi(p.getDescriptionHi());
        r.setLocationEn(p.getLocationEn());
        r.setLocationHi(p.getLocationHi());
        r.setProjectType(p.getProjectType());
        r.setStatus(p.getStatus());
        r.setAllocatedBudget(p.getAllocatedBudget());
        r.setTotalSpent(p.getTotalSpent());
        r.setRemainingBudget(p.getAllocatedBudget().subtract(p.getTotalSpent()));
        r.setProgressPercent(p.getProgressPercent());
        r.setStartDate(p.getStartDate());
        r.setExpectedEndDate(p.getExpectedEndDate());
        r.setActualEndDate(p.getActualEndDate());
        r.setIsPublicVisible(p.getIsPublicVisible());
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());

        if (p.getFund() != null) {
            r.setFundId(p.getFund().getId());
            r.setFundSchemeNameEn(p.getFund().getSchemeNameEn());
            r.setFundSchemeNameHi(p.getFund().getSchemeNameHi());
        }
        if (p.getAssignedOfficer() != null) {
            r.setAssignedOfficerId(p.getAssignedOfficer().getId());
            r.setAssignedOfficerName(p.getAssignedOfficer().getFullName());
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
