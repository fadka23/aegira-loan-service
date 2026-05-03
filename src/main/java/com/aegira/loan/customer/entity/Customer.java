package com.aegira.loan.customer.entity;

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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @Column(nullable = false, unique = true, length = 16)
    private String nik;
    @Column(nullable = false)
    private String name;
    private String phoneNumber;
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    @Column(nullable = false)
    private String address;
    private String maritalStatus;
    private String jobType;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyExpense;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal existingInstallment;
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
