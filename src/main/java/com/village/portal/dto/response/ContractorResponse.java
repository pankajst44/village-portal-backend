package com.village.portal.dto.response;


import com.village.portal.enums.ContractorCategory;

import java.time.LocalDateTime;

public class ContractorResponse {

    private Long id;
    private String nameEn;
    private String nameHi;
    private String registrationNumber;
    private String panNumber;
    private String gstNumber;
    private String contactPerson;
    private String phone;
    private String email;
    private String addressEn;
    private String addressHi;
    private ContractorCategory category;
    private Boolean isBlacklisted;
    private String blacklistReason;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ContractorResponse() {}

    public Long getId()                           { return id; }
    public void setId(Long v)                     { this.id = v; }

    public String getNameEn()                     { return nameEn; }
    public void setNameEn(String v)               { this.nameEn = v; }

    public String getNameHi()                     { return nameHi; }
    public void setNameHi(String v)               { this.nameHi = v; }

    public String getRegistrationNumber()         { return registrationNumber; }
    public void setRegistrationNumber(String v)   { this.registrationNumber = v; }

    public String getPanNumber()                  { return panNumber; }
    public void setPanNumber(String v)            { this.panNumber = v; }

    public String getGstNumber()                  { return gstNumber; }
    public void setGstNumber(String v)            { this.gstNumber = v; }

    public String getContactPerson()              { return contactPerson; }
    public void setContactPerson(String v)        { this.contactPerson = v; }

    public String getPhone()                      { return phone; }
    public void setPhone(String v)                { this.phone = v; }

    public String getEmail()                      { return email; }
    public void setEmail(String v)                { this.email = v; }

    public String getAddressEn()                  { return addressEn; }
    public void setAddressEn(String v)            { this.addressEn = v; }

    public String getAddressHi()                  { return addressHi; }
    public void setAddressHi(String v)            { this.addressHi = v; }

    public ContractorCategory getCategory()       { return category; }
    public void setCategory(ContractorCategory v) { this.category = v; }

    public Boolean getIsBlacklisted()             { return isBlacklisted; }
    public void setIsBlacklisted(Boolean v)       { this.isBlacklisted = v; }

    public String getBlacklistReason()            { return blacklistReason; }
    public void setBlacklistReason(String v)      { this.blacklistReason = v; }

    public String getCreatedByUsername()          { return createdByUsername; }
    public void setCreatedByUsername(String v)    { this.createdByUsername = v; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime v)     { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()           { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)     { this.updatedAt = v; }
}
