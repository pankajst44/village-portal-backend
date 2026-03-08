package com.village.portal.service;

import com.village.portal.dto.request.CreateContractorRequest;
import com.village.portal.dto.response.ContractorResponse;
import com.village.portal.enums.ContractorCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContractorService {

    ContractorResponse createContractor(CreateContractorRequest request);

    ContractorResponse getContractorById(Long id);

    Page<ContractorResponse> getAllContractors(Pageable pageable);

    Page<ContractorResponse> getActiveContractors(Pageable pageable);

    List<ContractorResponse> getContractorsByCategory(ContractorCategory category);

    List<ContractorResponse> getBlacklistedContractors();

    ContractorResponse updateContractor(Long id, CreateContractorRequest request);

    ContractorResponse blacklistContractor(Long id, String reason);

    ContractorResponse removeFromBlacklist(Long id);

    void deleteContractor(Long id);
}