package com.village.portal.service;

import com.village.portal.dto.request.CreateExpenditureRequest;
import com.village.portal.dto.response.ExpenditureResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ExpenditureService {

    ExpenditureResponse recordExpenditure(CreateExpenditureRequest request);

    ExpenditureResponse getExpenditureById(Long id);

    Page<ExpenditureResponse> getExpendituresByProject(Long projectId, Pageable pageable);

    List<ExpenditureResponse> getExpendituresByProjectAndDateRange(
            Long projectId, LocalDate from, LocalDate to);

    ExpenditureResponse verifyExpenditure(Long id);

    List<ExpenditureResponse> getPendingVerifications();
}
