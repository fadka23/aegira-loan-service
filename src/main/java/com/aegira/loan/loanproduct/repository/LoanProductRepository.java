package com.aegira.loan.loanproduct.repository;

import com.aegira.loan.loanproduct.entity.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoanProductRepository extends JpaRepository<LoanProduct, UUID> {
}
