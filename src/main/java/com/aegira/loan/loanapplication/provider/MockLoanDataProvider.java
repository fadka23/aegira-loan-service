package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class MockLoanDataProvider implements LoanDataProvider {
    @Override
    public Customer getCustomerById(UUID customerId) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setNik("3171234567890001");
        customer.setName("Mock Customer");
        customer.setPhoneNumber("081234567890");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 10));
        customer.setAddress("Mock Address");
        customer.setMaritalStatus("MARRIED");
        customer.setJobType("EMPLOYEE");
        customer.setMonthlyIncome(new BigDecimal("8000000.00"));
        customer.setMonthlyExpense(new BigDecimal("2500000.00"));
        customer.setExistingInstallment(new BigDecimal("1000000.00"));
        return customer;
    }

    @Override
    public LoanProduct getActiveLoanProductById(UUID loanProductId) {
        LoanProduct product = new LoanProduct();
        product.setId(loanProductId);
        product.setName("Personal Loan Mock");
        product.setMinAmount(new BigDecimal("5000000.00"));
        product.setMaxAmount(new BigDecimal("100000000.00"));
        product.setMinTenure(6);
        product.setMaxTenure(36);
        product.setAnnualInterestRate(new BigDecimal("0.1200"));
        product.setMinimumIncome(new BigDecimal("3000000.00"));
        product.setMaximumDsr(new BigDecimal("40.0000"));
        product.setNeedCollateral(false);
        product.setActive(true);
        return product;
    }

    @Override
    public boolean hasActiveApplication(UUID customerId) {
        return false;
    }
}
