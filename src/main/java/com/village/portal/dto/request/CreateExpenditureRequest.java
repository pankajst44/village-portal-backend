package com.village.portal.dto.request;

import com.village.portal.enums.PaymentMode;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateExpenditureRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long contractorId;

    @Size(max = 100)
    private String voucherNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @Size(max = 100)
    private String paymentReference;

    private String descriptionEn;
    private String descriptionHi;

    @NotBlank(message = "Financial year is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}$",
             message = "Financial year must be in format YYYY-YY (e.g. 2024-25)")
    private String financialYear;

    public CreateExpenditureRequest() {}

    public Long getProjectId()               { return projectId; }
    public void setProjectId(Long v)         { this.projectId = v; }

    public Long getContractorId()            { return contractorId; }
    public void setContractorId(Long v)      { this.contractorId = v; }

    public String getVoucherNumber()         { return voucherNumber; }
    public void setVoucherNumber(String v)   { this.voucherNumber = v; }

    public BigDecimal getAmount()            { return amount; }
    public void setAmount(BigDecimal v)      { this.amount = v; }

    public LocalDate getPaymentDate()        { return paymentDate; }
    public void setPaymentDate(LocalDate v)  { this.paymentDate = v; }

    public PaymentMode getPaymentMode()      { return paymentMode; }
    public void setPaymentMode(PaymentMode v){ this.paymentMode = v; }

    public String getPaymentReference()      { return paymentReference; }
    public void setPaymentReference(String v){ this.paymentReference = v; }

    public String getDescriptionEn()         { return descriptionEn; }
    public void setDescriptionEn(String v)   { this.descriptionEn = v; }

    public String getDescriptionHi()         { return descriptionHi; }
    public void setDescriptionHi(String v)   { this.descriptionHi = v; }

    public String getFinancialYear()         { return financialYear; }
    public void setFinancialYear(String v)   { this.financialYear = v; }
}
