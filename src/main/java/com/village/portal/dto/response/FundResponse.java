package com.village.portal.dto.response;

import com.village.portal.enums.FundSource;
import com.village.portal.enums.FundStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FundResponse {

    private Long id;
    private String schemeNameEn;
    private String schemeNameHi;
    private FundSource fundSource;
    private BigDecimal totalAmount;
    private BigDecimal amountReceived;
    private LocalDate releaseDate;
    private String financialYear;
    private String referenceNumber;
    private String descriptionEn;
    private String descriptionHi;
    private FundStatus status;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FundResponse() {}

    public Long getId()                       { return id; }
    public void setId(Long v)                 { this.id = v; }

    public String getSchemeNameEn()           { return schemeNameEn; }
    public void setSchemeNameEn(String v)     { this.schemeNameEn = v; }

    public String getSchemeNameHi()           { return schemeNameHi; }
    public void setSchemeNameHi(String v)     { this.schemeNameHi = v; }

    public FundSource getFundSource()         { return fundSource; }
    public void setFundSource(FundSource v)   { this.fundSource = v; }

    public BigDecimal getTotalAmount()        { return totalAmount; }
    public void setTotalAmount(BigDecimal v)  { this.totalAmount = v; }

    public BigDecimal getAmountReceived()     { return amountReceived; }
    public void setAmountReceived(BigDecimal v){ this.amountReceived = v; }

    public LocalDate getReleaseDate()         { return releaseDate; }
    public void setReleaseDate(LocalDate v)   { this.releaseDate = v; }

    public String getFinancialYear()          { return financialYear; }
    public void setFinancialYear(String v)    { this.financialYear = v; }

    public String getReferenceNumber()        { return referenceNumber; }
    public void setReferenceNumber(String v)  { this.referenceNumber = v; }

    public String getDescriptionEn()          { return descriptionEn; }
    public void setDescriptionEn(String v)    { this.descriptionEn = v; }

    public String getDescriptionHi()          { return descriptionHi; }
    public void setDescriptionHi(String v)    { this.descriptionHi = v; }

    public FundStatus getStatus()             { return status; }
    public void setStatus(FundStatus v)       { this.status = v; }

    public String getCreatedByUsername()      { return createdByUsername; }
    public void setCreatedByUsername(String v){ this.createdByUsername = v; }

    public LocalDateTime getCreatedAt()       { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()       { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
