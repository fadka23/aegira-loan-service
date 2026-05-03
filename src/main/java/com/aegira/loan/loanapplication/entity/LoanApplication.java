package com.aegira.loan.loanapplication.entity;

import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.user.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Version;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "loan_applications")
public class LoanApplication {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String applicationNumber;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private User agent;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id", nullable = false)
    private LoanProduct loanProduct;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal requestedAmount;
    @Column(nullable = false)
    private Integer requestedTenure;
    @Column(nullable = false)
    private String loanPurpose;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;
    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;
    private OffsetDateTime submittedAt;
    @Column(nullable = false)
    private OffsetDateTime createdAt;
    @Column(nullable = false)
    private OffsetDateTime updatedAt;
    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
