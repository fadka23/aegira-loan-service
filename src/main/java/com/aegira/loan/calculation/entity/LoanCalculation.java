package com.aegira.loan.calculation.entity;

import com.aegira.loan.loanapplication.entity.LoanApplication;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "loan_calculations")
public class LoanCalculation {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal annualInterestRate;
    @Column(nullable = false)
    private Integer tenureMonth;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalInterest;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayment;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal existingInstallment;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal currentDsr;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal projectedDsr;
    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal maximumDsr;
    @Column(nullable = false)
    private Boolean eligible;
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
