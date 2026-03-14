package com.village.portal.complaint.dto.request;

import com.village.portal.complaint.enums.ComplaintPriority;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class SubmitComplaintRequest {

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Ward number is required")
    @Min(1) @Max(20)
    private Integer wardNumber;

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 200, message = "Title must be 10–200 characters")
    private String titleEn;

    @Size(max = 200)
    private String titleHi;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 5000, message = "Description must be at least 20 characters")
    private String descriptionEn;

    @Size(max = 5000)
    private String descriptionHi;

    @NotBlank(message = "Location description is required")
    @Size(min = 5, max = 300)
    private String locationText;

    @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal latitude;

    @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal longitude;

    private ComplaintPriority priority;

    private Boolean isAnonymousDisplay = false;

    public Long getCategoryId()                 { return categoryId; }
    public void setCategoryId(Long v)           { this.categoryId = v; }
    public Integer getWardNumber()              { return wardNumber; }
    public void setWardNumber(Integer v)        { this.wardNumber = v; }
    public String getTitleEn()                  { return titleEn; }
    public void setTitleEn(String v)            { this.titleEn = v; }
    public String getTitleHi()                  { return titleHi; }
    public void setTitleHi(String v)            { this.titleHi = v; }
    public String getDescriptionEn()            { return descriptionEn; }
    public void setDescriptionEn(String v)      { this.descriptionEn = v; }
    public String getDescriptionHi()            { return descriptionHi; }
    public void setDescriptionHi(String v)      { this.descriptionHi = v; }
    public String getLocationText()             { return locationText; }
    public void setLocationText(String v)       { this.locationText = v; }
    public BigDecimal getLatitude()             { return latitude; }
    public void setLatitude(BigDecimal v)       { this.latitude = v; }
    public BigDecimal getLongitude()            { return longitude; }
    public void setLongitude(BigDecimal v)      { this.longitude = v; }
    public ComplaintPriority getPriority()      { return priority; }
    public void setPriority(ComplaintPriority v){ this.priority = v; }
    public Boolean getIsAnonymousDisplay()      { return isAnonymousDisplay; }
    public void setIsAnonymousDisplay(Boolean v){ this.isAnonymousDisplay = v; }
}
