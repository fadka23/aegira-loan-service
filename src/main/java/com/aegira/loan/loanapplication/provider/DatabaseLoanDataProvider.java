package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.customer.repository.CustomerRepository;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.loanproduct.repository.LoanProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatabaseLoanDataProvider implements LoanDataProvider {
    private final CustomerRepository customerRepository;
    private final LoanProductRepository loanProductRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    @Override
    public Customer getCustomerById(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    @Override
    public LoanProduct getActiveLoanProductById(UUID loanProductId) {
        LoanProduct product = loanProductRepository.findById(loanProductId)
                .orElseThrow(() -> new NotFoundException("Loan product not found"));
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new BadRequestException("Loan product is inactive");
        }
        return product;
    }

    @Override
    public boolean hasActiveApplication(UUID customerId) {
        Collection<ApplicationStatus> active = Arrays.asList(
                ApplicationStatus.WAITING_RISK_REVIEW,
                ApplicationStatus.RISK_APPROVED,
                ApplicationStatus.REVISION_REQUESTED,
                ApplicationStatus.WAITING_HO_APPROVAL,
                ApplicationStatus.HO_APPROVED
        );
        return loanApplicationRepository.existsActiveByCustomerId(customerId, active);
    }
}
