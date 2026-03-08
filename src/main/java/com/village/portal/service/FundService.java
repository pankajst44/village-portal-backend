package com.village.portal.service;

import com.village.portal.dto.request.CreateFundRequest;
import com.village.portal.dto.response.FundResponse;
import com.village.portal.enums.FundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FundService {

    FundResponse createFund(CreateFundRequest request);

    FundResponse getFundById(Long id);

    Page<FundResponse> getAllFunds(Pageable pageable);

    List<FundResponse> getFundsByStatus(FundStatus status);

    List<FundResponse> getFundsByFinancialYear(String year);

    FundResponse updateFund(Long id, CreateFundRequest request);

    void deleteFund(Long id);
}
