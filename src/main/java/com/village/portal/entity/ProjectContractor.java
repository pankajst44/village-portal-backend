package com.village.portal.entity;

import com.village.portal.enums.ContractStatus;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_contractors",
       uniqueConstraints = @UniqueConstraint(
           name = "uq_project_contractors_assignment",
           columnNames = {"project_id", "contractor_id"}
       ))
public class ProjectContractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contractor_id", nullable = false)
    private Contractor contractor;

    @Column(name = "work_order_number", length = 100)
    private String workOrderNumber;

    @Column(name = "contract_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal contractAmount = BigDecimal.ZERO;

    @Column(name = "work_start_date")
    private LocalDate workStartDate;

    @Column(name = "work_end_date")
    private LocalDate workEndDate;

    @Column(name = "work_description_en", columnDefinition = "TEXT")
    private String workDescriptionEn;

    @Column(name = "work_description_hi", columnDefinition = "TEXT")
    private String workDescriptionHi;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "ENUM('AWARDED','ONGOING','COMPLETED','TERMINATED')"
    )
    private ContractStatus status = ContractStatus.AWARDED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──

    public ProjectContractor() {}

    // ── Getters and Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public Contractor getContractor() { return contractor; }
    public void setContractor(Contractor contractor) { this.contractor = contractor; }

    public String getWorkOrderNumber() { return workOrderNumber; }
    public void setWorkOrderNumber(String workOrderNumber) { this.workOrderNumber = workOrderNumber; }

    public BigDecimal getContractAmount() { return contractAmount; }
    public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }

    public LocalDate getWorkStartDate() { return workStartDate; }
    public void setWorkStartDate(LocalDate workStartDate) { this.workStartDate = workStartDate; }

    public LocalDate getWorkEndDate() { return workEndDate; }
    public void setWorkEndDate(LocalDate workEndDate) { this.workEndDate = workEndDate; }

    public String getWorkDescriptionEn() { return workDescriptionEn; }
    public void setWorkDescriptionEn(String workDescriptionEn) { this.workDescriptionEn = workDescriptionEn; }

    public String getWorkDescriptionHi() { return workDescriptionHi; }
    public void setWorkDescriptionHi(String workDescriptionHi) { this.workDescriptionHi = workDescriptionHi; }

    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
