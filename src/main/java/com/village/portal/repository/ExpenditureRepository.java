package com.village.portal.repository;

import com.village.portal.entity.Expenditure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    Page<Expenditure> findByProjectId(Long projectId, Pageable pageable);

    List<Expenditure> findByProjectIdAndFinancialYear(Long projectId, String financialYear);

    List<Expenditure> findByContractorId(Long contractorId);

    List<Expenditure> findByIsVerified(Boolean isVerified);

    @Query("SELECT SUM(e.amount) FROM Expenditure e WHERE e.project.id = :projectId")
    BigDecimal sumAmountByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT e FROM Expenditure e WHERE e.project.id = :projectId " +
           "AND e.paymentDate BETWEEN :from AND :to")
    List<Expenditure> findByProjectIdAndDateRange(
            @Param("projectId") Long projectId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("SELECT SUM(e.amount) FROM Expenditure e WHERE e.financialYear = :year")
    BigDecimal sumAmountByFinancialYear(@Param("year") String year);
}
