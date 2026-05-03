package com.aegira.loan.eligibility;

import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.common.config.DataSourceMode;
import com.aegira.loan.common.config.LoanDataSourceProperties;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.repository.EligibilityResultRepository;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.provider.DatabaseLoanDataProvider;
import com.aegira.loan.loanapplication.provider.LoanDataProviderResolver;
import com.aegira.loan.loanapplication.provider.MockLoanDataProvider;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class EligibilityServiceMockModeTest {
    @Test
    void checkRulesUsingMockMode() {
        MockLoanDataProvider mockProvider = new MockLoanDataProvider();
        LoanDataSourceProperties properties = new LoanDataSourceProperties();
        properties.setMode(DataSourceMode.MOCK);
        LoanDataProviderResolver resolver = new LoanDataProviderResolver(
                properties, mock(DatabaseLoanDataProvider.class), mockProvider);
        EligibilityService service = new EligibilityService(mock(EligibilityResultRepository.class), resolver);
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        application.setCustomer(mockProvider.getCustomerById(customerId));
        application.setLoanProduct(mockProvider.getActiveLoanProductById(productId));
        application.setRequestedAmount(new BigDecimal("25000000.00"));
        application.setRequestedTenure(12);
        LoanCalculation calculation = new LoanCalculation();
        calculation.setProjectedDsr(new BigDecimal("38.7500"));

        List<EligibilityResult> results = service.evaluate(application, calculation);

        assertTrue(results.stream().allMatch(EligibilityResult::getPassed));
    }
}
