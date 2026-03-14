package com.village.portal.complaint.dto.response;

import com.village.portal.complaint.enums.ComplaintPriority;

public class CategoryResponse {
    private Long id;
    private String nameEn;
    private String nameHi;
    private String descriptionEn;
    private Integer slaDays;
    private Integer escalationAfterDays;
    private ComplaintPriority defaultPriority;
    private Integer displayOrder;

    public Long getId()                             { return id; }
    public void setId(Long v)                       { this.id = v; }
    public String getNameEn()                       { return nameEn; }
    public void setNameEn(String v)                 { this.nameEn = v; }
    public String getNameHi()                       { return nameHi; }
    public void setNameHi(String v)                 { this.nameHi = v; }
    public String getDescriptionEn()                { return descriptionEn; }
    public void setDescriptionEn(String v)          { this.descriptionEn = v; }
    public Integer getSlaDays()                     { return slaDays; }
    public void setSlaDays(Integer v)               { this.slaDays = v; }
    public Integer getEscalationAfterDays()         { return escalationAfterDays; }
    public void setEscalationAfterDays(Integer v)   { this.escalationAfterDays = v; }
    public ComplaintPriority getDefaultPriority()   { return defaultPriority; }
    public void setDefaultPriority(ComplaintPriority v){ this.defaultPriority = v; }
    public Integer getDisplayOrder()                { return displayOrder; }
    public void setDisplayOrder(Integer v)          { this.displayOrder = v; }
}
