package com.aegira.loan.eligibility.repository;

import com.aegira.loan.eligibility.entity.EligibilityResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EligibilityResultRepository extends JpaRepository<EligibilityResult, UUID> {
    List<EligibilityResult> findByLoanApplicationId(UUID loanApplicationId);
    void deleteByLoanApplicationId(UUID loanApplicationId);
}
