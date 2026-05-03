package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.loanproduct.entity.LoanProduct;

import java.util.UUID;

public interface LoanDataProvider {
    Customer getCustomerById(UUID customerId);
    LoanProduct getActiveLoanProductById(UUID loanProductId);
    boolean hasActiveApplication(UUID customerId);
}
