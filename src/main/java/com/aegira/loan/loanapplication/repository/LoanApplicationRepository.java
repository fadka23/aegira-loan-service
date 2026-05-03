package com.aegira.loan.loanapplication.repository;

import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {
    List<LoanApplication> findByAgentId(UUID agentId);
    List<LoanApplication> findByStatusNot(ApplicationStatus status);
    List<LoanApplication> findByStatus(ApplicationStatus status);
    long countByAgentIdAndStatus(UUID agentId, ApplicationStatus status);
    long countByStatus(ApplicationStatus status);
    long countByRiskLevel(com.aegira.loan.loanapplication.entity.RiskLevel riskLevel);
    long countByStatusAndRiskLevel(ApplicationStatus status, com.aegira.loan.loanapplication.entity.RiskLevel riskLevel);
    long countByStatusAndUpdatedAtBetween(ApplicationStatus status, OffsetDateTime from, OffsetDateTime to);

    @Query("select count(a) > 0 from LoanApplication a where a.customer.id = :customerId and a.id <> :excludedId and a.status in :statuses")
    boolean existsActiveByCustomer(@Param("customerId") UUID customerId,
                                   @Param("excludedId") UUID excludedId,
                                   @Param("statuses") Collection<ApplicationStatus> statuses);

    @Query("select count(a) > 0 from LoanApplication a where a.customer.id = :customerId and a.status in :statuses")
    boolean existsActiveByCustomerId(@Param("customerId") UUID customerId,
                                     @Param("statuses") Collection<ApplicationStatus> statuses);
}
