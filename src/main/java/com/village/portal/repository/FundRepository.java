package com.village.portal.repository;

import com.village.portal.entity.Fund;
import com.village.portal.enums.FundSource;
import com.village.portal.enums.FundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {

    Optional<Fund> findByReferenceNumber(String referenceNumber);

    boolean existsByReferenceNumber(String referenceNumber);

    List<Fund> findByStatus(FundStatus status);

    List<Fund> findByFinancialYear(String financialYear);

    List<Fund> findByFundSource(FundSource fundSource);

    Page<Fund> findByStatus(FundStatus status, Pageable pageable);

    @Query("SELECT SUM(f.amountReceived) FROM Fund f WHERE f.status = 'ACTIVE'")
    BigDecimal sumActiveReceivedAmount();

    @Query("SELECT f FROM Fund f WHERE f.financialYear = :year AND f.status = :status")
    List<Fund> findByFinancialYearAndStatus(
            @Param("year") String year,
            @Param("status") FundStatus status);
}
