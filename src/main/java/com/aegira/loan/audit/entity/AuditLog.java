package com.aegira.loan.audit.entity;

import com.aegira.loan.user.entity.User;
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
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    @Column(nullable = false)
    private String entityType;
    @Column(nullable = false)
    private UUID entityId;
    @Column(nullable = false)
    private String action;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    @Column(columnDefinition = "TEXT")
    private String newValue;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(nullable = false)
    private String correlationId;
    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = OffsetDateTime.now();
    }
}
