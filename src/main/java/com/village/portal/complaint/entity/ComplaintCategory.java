package com.village.portal.complaint.entity;

import com.village.portal.complaint.enums.ComplaintPriority;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_categories")
public class ComplaintCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_hi", length = 100)
    private String nameHi;

    @Column(name = "description_en", length = 300)
    private String descriptionEn;

    @Column(name = "sla_days", nullable = false)
    private Integer slaDays;

    @Column(name = "escalation_after_days", nullable = false)
    private Integer escalationAfterDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_priority", nullable = false, length = 10)
    private ComplaintPriority defaultPriority;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() { this.updatedAt = LocalDateTime.now(); }

    public ComplaintCategory() {}

    public Long getId()                         { return id; }
    public String getNameEn()                   { return nameEn; }
    public void setNameEn(String v)             { this.nameEn = v; }
    public String getNameHi()                   { return nameHi; }
    public void setNameHi(String v)             { this.nameHi = v; }
    public String getDescriptionEn()            { return descriptionEn; }
    public void setDescriptionEn(String v)      { this.descriptionEn = v; }
    public Integer getSlaDays()                 { return slaDays; }
    public void setSlaDays(Integer v)           { this.slaDays = v; }
    public Integer getEscalationAfterDays()     { return escalationAfterDays; }
    public void setEscalationAfterDays(Integer v){ this.escalationAfterDays = v; }
    public ComplaintPriority getDefaultPriority(){ return defaultPriority; }
    public void setDefaultPriority(ComplaintPriority v){ this.defaultPriority = v; }
    public Integer getDisplayOrder()            { return displayOrder; }
    public void setDisplayOrder(Integer v)      { this.displayOrder = v; }
    public Boolean getIsActive()                { return isActive; }
    public void setIsActive(Boolean v)          { this.isActive = v; }
    public LocalDateTime getCreatedAt()         { return createdAt; }
    public LocalDateTime getUpdatedAt()         { return updatedAt; }
}
