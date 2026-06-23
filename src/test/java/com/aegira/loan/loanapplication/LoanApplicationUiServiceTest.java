package com.aegira.loan.loanapplication;

import com.aegira.loan.approval.entity.ApprovalDecision;
import com.aegira.loan.approval.entity.ApprovalHistory;
import com.aegira.loan.approval.repository.ApprovalHistoryRepository;
import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.calculation.repository.LoanCalculationRepository;
import com.aegira.loan.calculation.service.LoanCalculationService;
import com.aegira.loan.common.exception.ForbiddenException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.entity.EligibilityRule;
import com.aegira.loan.eligibility.repository.EligibilityResultRepository;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.dto.LoanApplicationDetailResponse;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import com.aegira.loan.loanapplication.provider.LoanDataProviderResolver;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.loanapplication.service.LoanApplicationService;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LoanApplicationUiServiceTest {
    private LoanApplicationRepository applicationRepository;
    private LoanCalculationRepository calculationRepository;
    private EligibilityResultRepository eligibilityRepository;
    private ApprovalHistoryRepository approvalHistoryRepository;
    private SecurityUtil securityUtil;
    private LoanApplicationService service;

    @BeforeEach
    void setUp() {
        applicationRepository = mock(LoanApplicationRepository.class);
        calculationRepository = mock(LoanCalculationRepository.class);
        eligibilityRepository = mock(EligibilityResultRepository.class);
        approvalHistoryRepository = mock(ApprovalHistoryRepository.class);
        securityUtil = mock(SecurityUtil.class);
        service = new LoanApplicationService(
                applicationRepository,
                mock(LoanDataProviderResolver.class),
                securityUtil,
                mock(LoanCalculationService.class),
                calculationRepository,
                mock(EligibilityService.class),
                eligibilityRepository,
                approvalHistoryRepository,
                mock(AuditService.class)
        );
    }

    @Test
    void applicationDetailReturnsAggregateData() {
        User agent = user(Role.AGENT);
        LoanApplication application = application(agent);
        when(securityUtil.currentUser()).thenReturn(agent);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        when(calculationRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(application.getId())).thenReturn(Optional.of(calculation(application)));
        when(eligibilityRepository.findByLoanApplicationId(application.getId())).thenReturn(Collections.singletonList(eligibility(application)));
        when(approvalHistoryRepository.findByLoanApplicationIdOrderByCreatedAtAsc(application.getId())).thenReturn(Collections.singletonList(history(application)));

        LoanApplicationDetailResponse detail = service.detail(application.getId());

        assertEquals(application.getId(), detail.getId());
        assertEquals("Budi Santoso", detail.getCustomer().getName());
        assertEquals("Personal Loan", detail.getLoanProduct().getName());
        assertEquals(new BigDecimal("1550000.00"), detail.getCalculation().getMonthlyInstallment());
        assertEquals("MINIMUM_INCOME", detail.getEligibilityResults().get(0).getRuleCode());
        assertEquals("APPROVED", detail.getApprovalHistories().get(0).getDecision());
    }

    @Test
    void agentCannotAccessAnotherAgentApplicationDetail() {
        User owner = user(Role.AGENT);
        User otherAgent = user(Role.AGENT);
        LoanApplication application = application(owner);
        when(securityUtil.currentUser()).thenReturn(otherAgent);
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        assertThrows(ForbiddenException.class, () -> service.detail(application.getId()));
        verifyNoInteractions(calculationRepository, eligibilityRepository, approvalHistoryRepository);
    }

    private LoanApplication application(User agent) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setNik("3171234567890001");
        customer.setName("Budi Santoso");
        customer.setPhoneNumber("08123456789");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 10));
        customer.setMonthlyIncome(new BigDecimal("8000000.00"));
        customer.setMonthlyExpense(new BigDecimal("2500000.00"));
        customer.setExistingInstallment(new BigDecimal("1000000.00"));
        LoanProduct product = new LoanProduct();
        product.setId(UUID.randomUUID());
        product.setName("Personal Loan");
        product.setAnnualInterestRate(new BigDecimal("0.1200"));
        product.setMaximumDsr(new BigDecimal("40.0000"));
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        application.setApplicationNumber("APP-20260503-0001");
        application.setCustomer(customer);
        application.setAgent(agent);
        application.setLoanProduct(product);
        application.setRequestedAmount(new BigDecimal("30000000.00"));
        application.setRequestedTenure(24);
        application.setLoanPurpose("Working Capital");
        application.setStatus(ApplicationStatus.WAITING_RISK_REVIEW);
        application.setRiskLevel(RiskLevel.MEDIUM);
        application.setCreatedAt(OffsetDateTime.now());
        return application;
    }

    private LoanCalculation calculation(LoanApplication application) {
        LoanCalculation calculation = new LoanCalculation();
        calculation.setLoanApplication(application);
        calculation.setTotalInterest(new BigDecimal("7200000.00"));
        calculation.setTotalPayment(new BigDecimal("37200000.00"));
        calculation.setMonthlyInstallment(new BigDecimal("1550000.00"));
        calculation.setCurrentDsr(new BigDecimal("12.5000"));
        calculation.setProjectedDsr(new BigDecimal("31.8750"));
        calculation.setEligible(true);
        return calculation;
    }

    private EligibilityResult eligibility(LoanApplication application) {
        EligibilityResult result = new EligibilityResult();
        result.setLoanApplication(application);
        result.setRuleName(EligibilityRule.MINIMUM_INCOME);
        result.setPassed(true);
        result.setMessage("Customer income meets minimum requirement");
        return result;
    }

    private ApprovalHistory history(LoanApplication application) {
        ApprovalHistory history = new ApprovalHistory();
        history.setLoanApplication(application);
        history.setPerformedBy(user(Role.RISK));
        history.setDecision(ApprovalDecision.RISK_APPROVE);
        history.setNotes("Customer is eligible");
        history.setCreatedAt(OffsetDateTime.now());
        return history;
    }

    private User user(Role role) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(role.name().toLowerCase() + "@aegira.com");
        user.setRole(role);
        return user;
    }
}
