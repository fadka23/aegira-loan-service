package com.aegira.loan.loanapplication;

import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.approval.repository.ApprovalHistoryRepository;
import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.calculation.repository.LoanCalculationRepository;
import com.aegira.loan.calculation.service.LoanCalculationService;
import com.aegira.loan.common.config.DataSourceMode;
import com.aegira.loan.common.config.LoanDataSourceProperties;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.repository.EligibilityResultRepository;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import com.aegira.loan.loanapplication.provider.DatabaseLoanDataProvider;
import com.aegira.loan.loanapplication.provider.LoanDataProviderResolver;
import com.aegira.loan.loanapplication.provider.MockLoanDataProvider;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.loanapplication.service.LoanApplicationService;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoanApplicationServiceMockModeTest {
    @Test
    void submitApplicationUsingMockMode() {
        LoanDataProviderResolver resolver = mockResolver();
        LoanCalculationRepository calculationRepository = mock(LoanCalculationRepository.class);
        EligibilityResultRepository eligibilityRepository = mock(EligibilityResultRepository.class);
        when(calculationRepository.save(any(LoanCalculation.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(eligibilityRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanCalculationService calculationService = new LoanCalculationService(calculationRepository);
        EligibilityService eligibilityService = new EligibilityService(eligibilityRepository, resolver);
        LoanApplicationRepository applicationRepository = mock(LoanApplicationRepository.class);
        SecurityUtil securityUtil = mock(SecurityUtil.class);
        AuditService auditService = mock(AuditService.class);
        User agent = new User();
        agent.setId(UUID.randomUUID());
        agent.setRole(Role.AGENT);
        when(securityUtil.currentUser()).thenReturn(agent);

        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LoanApplication application = draftApplication(customerId, productId, agent);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        LoanApplicationService service = new LoanApplicationService(
                applicationRepository,
                resolver,
                securityUtil,
                calculationService,
                calculationRepository,
                eligibilityService,
                eligibilityRepository,
                mock(ApprovalHistoryRepository.class),
                auditService);

        LoanApplicationResponse response = service.submit(application.getId());

        assertEquals(ApplicationStatus.WAITING_RISK_REVIEW, response.getStatus());
        assertEquals(RiskLevel.HIGH, response.getRiskLevel());
        assertNotNull(response.getSubmittedAt());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(productId, response.getLoanProductId());
    }

    private LoanDataProviderResolver mockResolver() {
        LoanDataSourceProperties properties = new LoanDataSourceProperties();
        properties.setMode(DataSourceMode.MOCK);
        return new LoanDataProviderResolver(properties, mock(DatabaseLoanDataProvider.class), new MockLoanDataProvider());
    }

    private LoanApplication draftApplication(UUID customerId, UUID productId, User agent) {
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setDateOfBirth(LocalDate.of(1988, 1, 1));
        LoanProduct product = new LoanProduct();
        product.setId(productId);
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        application.setApplicationNumber("LA-MOCK-001");
        application.setCustomer(customer);
        application.setAgent(agent);
        application.setLoanProduct(product);
        application.setRequestedAmount(new BigDecimal("25000000.00"));
        application.setRequestedTenure(12);
        application.setLoanPurpose("Mock flow test");
        application.setStatus(ApplicationStatus.DRAFT);
        return application;
    }
}
