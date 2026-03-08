package com.village.portal.repository;

import com.village.portal.entity.ProjectContractor;
import com.village.portal.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectContractorRepository extends JpaRepository<ProjectContractor, Long> {

    List<ProjectContractor> findByProjectId(Long projectId);

    List<ProjectContractor> findByContractorId(Long contractorId);

    Optional<ProjectContractor> findByProjectIdAndContractorId(Long projectId, Long contractorId);

    boolean existsByProjectIdAndContractorId(Long projectId, Long contractorId);

    List<ProjectContractor> findByProjectIdAndStatus(Long projectId, ContractStatus status);
}
