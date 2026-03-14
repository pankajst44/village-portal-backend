package com.village.portal.complaint.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "villages")
public class Village {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_hi", length = 100)
    private String nameHi;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "total_wards")
    private Integer totalWards;

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

    public Village() {}

    public Long getId()                 { return id; }
    public String getNameEn()           { return nameEn; }
    public void setNameEn(String v)     { this.nameEn = v; }
    public String getNameHi()           { return nameHi; }
    public void setNameHi(String v)     { this.nameHi = v; }
    public String getDistrict()         { return district; }
    public String getState()            { return state; }
    public Integer getTotalWards()      { return totalWards; }
    public Boolean getIsActive()        { return isActive; }
    public void setIsActive(Boolean v)  { this.isActive = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
