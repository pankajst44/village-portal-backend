package com.village.portal.repository;

import com.village.portal.entity.Project;
import com.village.portal.enums.ProjectStatus;
import com.village.portal.enums.ProjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByProjectCode(String projectCode);

    boolean existsByProjectCode(String projectCode);

    // Public dashboard — only visible, active projects
    Page<Project> findByIsPublicVisibleTrueAndStatus(ProjectStatus status, Pageable pageable);

    Page<Project> findByIsPublicVisibleTrue(Pageable pageable);

    List<Project> findByFundId(Long fundId);

    boolean existsByFundId(Long fundId);

    List<Project> findByAssignedOfficerId(Long officerId);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByProjectType(ProjectType projectType);

    @Query("SELECT p FROM Project p WHERE p.isPublicVisible = true " +
            "AND (:status IS NULL OR p.status = :status) " +
            "AND (:type IS NULL OR p.projectType = :type)")
    Page<Project> findPublicProjectsFiltered(
            @Param("status") ProjectStatus status,
            @Param("type") ProjectType type,
            Pageable pageable);

    @Query("SELECT p FROM Project p WHERE p.assignedOfficer.id = :officerId " +
            "AND (:status IS NULL OR p.status = :status)")
    List<Project> findOfficerProjectsFiltered(
            @Param("officerId") Long officerId,
            @Param("status") ProjectStatus status);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status")
    Long countByStatus(@Param("status") ProjectStatus status);
}