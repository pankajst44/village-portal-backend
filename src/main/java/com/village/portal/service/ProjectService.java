package com.village.portal.service;

import com.village.portal.dto.request.CreateProjectRequest;
import com.village.portal.dto.request.UpdateProjectProgressRequest;
import com.village.portal.dto.response.ProjectResponse;
import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse getProjectById(Long id);

    ProjectResponse getProjectByCode(String code);

    // Public dashboard — filtered, paginated
    Page<ProjectResponse> getPublicProjects(ProjectStatus status, ProjectType type, Pageable pageable);

    // Admin / officer view — all projects
    Page<ProjectResponse> getAllProjects(Pageable pageable);

    List<ProjectResponse> getProjectsByOfficer(Long officerId);

    ProjectResponse updateProject(Long id, CreateProjectRequest request);

    ProjectResponse updateProgress(Long id, UpdateProjectProgressRequest request);

    void deleteProject(Long id);
}
