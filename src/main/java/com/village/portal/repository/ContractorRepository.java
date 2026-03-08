package com.village.portal.repository;

import com.village.portal.entity.Contractor;
import com.village.portal.enums.ContractorCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractorRepository extends JpaRepository<Contractor, Long> {

    Optional<Contractor> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    boolean existsByPanNumber(String panNumber);

    List<Contractor> findByIsBlacklisted(Boolean isBlacklisted);

    List<Contractor> findByCategory(ContractorCategory category);

    Page<Contractor> findByIsBlacklistedFalse(Pageable pageable);
}
