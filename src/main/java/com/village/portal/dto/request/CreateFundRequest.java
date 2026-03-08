package com.village.portal.dto.request;

import com.village.portal.enums.FundSource;
import com.village.portal.enums.FundStatus;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateFundRequest {

    @NotBlank(message = "Scheme name (English) is required")
    @Size(max = 200, message = "Scheme name must not exceed 200 characters")
    private String schemeNameEn;

    @Size(max = 200, message = "Scheme name (Hindi) must not exceed 200 characters")
    private String schemeNameHi;

    @NotNull(message = "Fund source is required")
    private FundSource fundSource;

    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "Amount received cannot be negative")
    private BigDecimal amountReceived = BigDecimal.ZERO;

    private LocalDate releaseDate;

    @NotBlank(message = "Financial year is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Financial year must be in format YYYY-YY (e.g. 2024-25)")
    private String financialYear;

    @Size(max = 100, message = "Reference number must not exceed 100 characters")
    private String referenceNumber;

    private String descriptionEn;
    private String descriptionHi;

    private FundStatus status = FundStatus.PENDING;

    public CreateFundRequest() {}

    public String getSchemeNameEn()       { return schemeNameEn; }
    public void setSchemeNameEn(String v) { this.schemeNameEn = v; }

    public String getSchemeNameHi()       { return schemeNameHi; }
    public void setSchemeNameHi(String v) { this.schemeNameHi = v; }

    public FundSource getFundSource()            { return fundSource; }
    public void setFundSource(FundSource v)      { this.fundSource = v; }

    public BigDecimal getTotalAmount()           { return totalAmount; }
    public void setTotalAmount(BigDecimal v)     { this.totalAmount = v; }

    public BigDecimal getAmountReceived()        { return amountReceived; }
    public void setAmountReceived(BigDecimal v)  { this.amountReceived = v; }

    public LocalDate getReleaseDate()            { return releaseDate; }
    public void setReleaseDate(LocalDate v)      { this.releaseDate = v; }

    public String getFinancialYear()             { return financialYear; }
    public void setFinancialYear(String v)       { this.financialYear = v; }

    public String getReferenceNumber()           { return referenceNumber; }
    public void setReferenceNumber(String v)     { this.referenceNumber = v; }

    public String getDescriptionEn()             { return descriptionEn; }
    public void setDescriptionEn(String v)       { this.descriptionEn = v; }

    public String getDescriptionHi()             { return descriptionHi; }
    public void setDescriptionHi(String v)       { this.descriptionHi = v; }

    public FundStatus getStatus()                { return status; }
    public void setStatus(FundStatus v)          { this.status = v; }
}
