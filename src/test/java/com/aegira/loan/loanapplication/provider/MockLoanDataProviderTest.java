package com.aegira.loan.loanapplication.provider;

import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MockLoanDataProviderTest {
    private final MockLoanDataProvider provider = new MockLoanDataProvider();

    @Test
    void returnsMockCustomer() {
        UUID customerId = UUID.randomUUID();

        Customer customer = provider.getCustomerById(customerId);

        assertEquals(customerId, customer.getId());
        assertEquals("3171234567890001", customer.getNik());
        assertEquals("Mock Customer", customer.getName());
        assertEquals(new BigDecimal("8000000.00"), customer.getMonthlyIncome());
        assertEquals(new BigDecimal("2500000.00"), customer.getMonthlyExpense());
        assertEquals(new BigDecimal("1000000.00"), customer.getExistingInstallment());
        assertEquals(LocalDate.of(1990, 1, 10), customer.getDateOfBirth());
    }

    @Test
    void returnsMockLoanProduct() {
        UUID productId = UUID.randomUUID();

        LoanProduct product = provider.getActiveLoanProductById(productId);

        assertEquals(productId, product.getId());
        assertEquals("Personal Loan Mock", product.getName());
        assertEquals(new BigDecimal("5000000.00"), product.getMinAmount());
        assertEquals(new BigDecimal("100000000.00"), product.getMaxAmount());
        assertEquals(Integer.valueOf(6), product.getMinTenure());
        assertEquals(Integer.valueOf(36), product.getMaxTenure());
        assertEquals(new BigDecimal("0.1200"), product.getAnnualInterestRate());
        assertEquals(new BigDecimal("3000000.00"), product.getMinimumIncome());
        assertEquals(new BigDecimal("40.0000"), product.getMaximumDsr());
        assertEquals(false, product.getNeedCollateral());
        assertEquals(true, product.getActive());
    }

    @Test
    void duplicateActiveApplicationDefaultsToFalse() {
        assertFalse(provider.hasActiveApplication(UUID.randomUUID()));
    }
}
