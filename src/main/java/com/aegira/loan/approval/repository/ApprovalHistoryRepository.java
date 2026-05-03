package com.aegira.loan.approval.repository;

import com.aegira.loan.approval.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, UUID> {
    List<ApprovalHistory> findByLoanApplicationIdOrderByCreatedAtAsc(UUID loanApplicationId);
}
