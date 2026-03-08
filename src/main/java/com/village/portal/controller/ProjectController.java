package com.village.portal.controller;

import com.village.portal.dto.request.CreateProjectRequest;
import com.village.portal.dto.request.UpdateProjectProgressRequest;
import com.village.portal.dto.response.ApiResponse;
import com.village.portal.dto.response.ProjectResponse;
import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;
import com.village.portal.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // ── PUBLIC ENDPOINTS ──

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getPublicProjects(
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProjectResponse> result = projectService.getPublicProjects(status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getPublicProjectById(@PathVariable Long id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/public/code/{code}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectByCode(@PathVariable String code) {
        ProjectResponse response = projectService.getProjectByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── AUTHENTICATED ENDPOINTS ──

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getAllProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProjectResponse> result = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER','AUDITOR')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {

        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody CreateProjectRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Project updated", projectService.updateProject(id, request)));
    }

    @PatchMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('ADMIN','OFFICER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectProgressRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Progress updated", projectService.updateProgress(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }
}
