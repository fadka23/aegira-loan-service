package com.aegira.loan.loanproduct.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "loan_products")
public class LoanProduct {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minAmount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal maxAmount;
    @Column(nullable = false)
    private Integer minTenure;
    @Column(nullable = false)
    private Integer maxTenure;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal annualInterestRate;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumIncome;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal maximumDsr;
    @Column(nullable = false)
    private Boolean needCollateral;
    @Column(nullable = false)
    private Boolean active;
    @Column(nullable = false)
    private OffsetDateTime createdAt;
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

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
