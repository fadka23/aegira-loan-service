package com.aegira.loan.calculation.repository;

import com.aegira.loan.calculation.entity.LoanCalculation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanCalculationRepository extends JpaRepository<LoanCalculation, UUID> {
    Optional<LoanCalculation> findTopByLoanApplicationIdOrderByCreatedAtDesc(UUID loanApplicationId);
    List<LoanCalculation> findByLoanApplicationId(UUID loanApplicationId);
}
