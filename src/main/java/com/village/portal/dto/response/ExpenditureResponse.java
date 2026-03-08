package com.village.portal.dto.response;

import com.village.portal.enums.PaymentMode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenditureResponse {

    private Long id;
    private Long projectId;
    private String projectNameEn;
    private String projectNameHi;
    private Long contractorId;
    private String contractorNameEn;
    private String voucherNumber;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private PaymentMode paymentMode;
    private String paymentReference;
    private String descriptionEn;
    private String descriptionHi;
    private String financialYear;
    private Boolean isVerified;
    private String verifiedByUsername;
    private LocalDateTime verifiedAt;
    private String recordedByUsername;
    private LocalDateTime createdAt;

    public ExpenditureResponse() {}

    public Long getId()                          { return id; }
    public void setId(Long v)                    { this.id = v; }

    public Long getProjectId()                   { return projectId; }
    public void setProjectId(Long v)             { this.projectId = v; }

    public String getProjectNameEn()             { return projectNameEn; }
    public void setProjectNameEn(String v)       { this.projectNameEn = v; }

    public String getProjectNameHi()             { return projectNameHi; }
    public void setProjectNameHi(String v)       { this.projectNameHi = v; }

    public Long getContractorId()                { return contractorId; }
    public void setContractorId(Long v)          { this.contractorId = v; }

    public String getContractorNameEn()          { return contractorNameEn; }
    public void setContractorNameEn(String v)    { this.contractorNameEn = v; }

    public String getVoucherNumber()             { return voucherNumber; }
    public void setVoucherNumber(String v)       { this.voucherNumber = v; }

    public BigDecimal getAmount()                { return amount; }
    public void setAmount(BigDecimal v)          { this.amount = v; }

    public LocalDate getPaymentDate()            { return paymentDate; }
    public void setPaymentDate(LocalDate v)      { this.paymentDate = v; }

    public PaymentMode getPaymentMode()          { return paymentMode; }
    public void setPaymentMode(PaymentMode v)    { this.paymentMode = v; }

    public String getPaymentReference()          { return paymentReference; }
    public void setPaymentReference(String v)    { this.paymentReference = v; }

    public String getDescriptionEn()             { return descriptionEn; }
    public void setDescriptionEn(String v)       { this.descriptionEn = v; }

    public String getDescriptionHi()             { return descriptionHi; }
    public void setDescriptionHi(String v)       { this.descriptionHi = v; }

    public String getFinancialYear()             { return financialYear; }
    public void setFinancialYear(String v)       { this.financialYear = v; }

    public Boolean getIsVerified()               { return isVerified; }
    public void setIsVerified(Boolean v)         { this.isVerified = v; }

    public String getVerifiedByUsername()        { return verifiedByUsername; }
    public void setVerifiedByUsername(String v)  { this.verifiedByUsername = v; }

    public LocalDateTime getVerifiedAt()         { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime v)   { this.verifiedAt = v; }

    public String getRecordedByUsername()        { return recordedByUsername; }
    public void setRecordedByUsername(String v)  { this.recordedByUsername = v; }

    public LocalDateTime getCreatedAt()          { return createdAt; }
    public void setCreatedAt(LocalDateTime v)    { this.createdAt = v; }
}
