package com.village.portal.dto.request;

import com.village.portal.enums.ContractorCategory;

import javax.validation.constraints.*;

public class CreateContractorRequest {

    @NotBlank(message = "Contractor name (English) is required")
    @Size(max = 200)
    private String nameEn;

    @Size(max = 200)
    private String nameHi;

    @NotBlank(message = "Registration number is required")
    @Size(max = 100)
    private String registrationNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$",
             message = "Please provide a valid PAN number (e.g. ABCDE1234F)")
    private String panNumber;

    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
             message = "Please provide a valid GST number")
    private String gstNumber;

    @Size(max = 100)
    private String contactPerson;

    @Pattern(regexp = "^[6-9]\\d{9}$",
             message = "Please provide a valid 10-digit Indian mobile number")
    private String phone;

    @Email(message = "Please provide a valid email address")
    @Size(max = 100)
    private String email;

    private String addressEn;
    private String addressHi;

    @NotNull(message = "Contractor category is required")
    private ContractorCategory category;

    public CreateContractorRequest() {}

    public String getNameEn()                   { return nameEn; }
    public void setNameEn(String v)             { this.nameEn = v; }

    public String getNameHi()                   { return nameHi; }
    public void setNameHi(String v)             { this.nameHi = v; }

    public String getRegistrationNumber()       { return registrationNumber; }
    public void setRegistrationNumber(String v) { this.registrationNumber = v; }

    public String getPanNumber()                { return panNumber; }
    public void setPanNumber(String v)          { this.panNumber = v; }

    public String getGstNumber()                { return gstNumber; }
    public void setGstNumber(String v)          { this.gstNumber = v; }

    public String getContactPerson()            { return contactPerson; }
    public void setContactPerson(String v)      { this.contactPerson = v; }

    public String getPhone()                    { return phone; }
    public void setPhone(String v)              { this.phone = v; }

    public String getEmail()                    { return email; }
    public void setEmail(String v)              { this.email = v; }

    public String getAddressEn()                { return addressEn; }
    public void setAddressEn(String v)          { this.addressEn = v; }

    public String getAddressHi()                { return addressHi; }
    public void setAddressHi(String v)          { this.addressHi = v; }

    public ContractorCategory getCategory()     { return category; }
    public void setCategory(ContractorCategory v){ this.category = v; }
}
