package com.aegira.loan.eligibility.service;

import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.eligibility.dto.EligibilityResultResponse;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.entity.EligibilityRule;
import com.aegira.loan.eligibility.repository.EligibilityResultRepository;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import com.aegira.loan.loanapplication.provider.LoanDataProvider;
import com.aegira.loan.loanapplication.provider.LoanDataProviderResolver;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityService {
    private final EligibilityResultRepository eligibilityResultRepository;
    private final LoanDataProviderResolver loanDataProviderResolver;

    public List<EligibilityResult> evaluateAndSave(LoanApplication application, LoanCalculation calculation) {
        eligibilityResultRepository.deleteByLoanApplicationId(application.getId());
        List<EligibilityResult> results = evaluate(application, calculation);
        log.info("eligibility evaluated applicationId={} eligible={}", application.getId(),
                results.stream().allMatch(EligibilityResult::getPassed));
        return eligibilityResultRepository.saveAll(results);
    }

    public List<EligibilityResult> evaluate(LoanApplication application, LoanCalculation calculation) {
        Customer customer = application.getCustomer();
        LoanProduct product = application.getLoanProduct();
        LocalDate today = LocalDate.now();
        int age = Period.between(customer.getDateOfBirth(), today).getYears();
        int maturityAge = Period.between(customer.getDateOfBirth(), today.plusMonths(application.getRequestedTenure())).getYears();
        return Arrays.asList(
                result(application, EligibilityRule.MINIMUM_AGE, age >= 21, "Customer age must be >= 21"),
                result(application, EligibilityRule.MAXIMUM_AGE_AT_MATURITY, maturityAge <= 60, "Customer age at maturity must be <= 60"),
                result(application, EligibilityRule.MINIMUM_INCOME, customer.getMonthlyIncome().compareTo(product.getMinimumIncome()) >= 0, "Monthly income must meet product minimum"),
                result(application, EligibilityRule.LOAN_AMOUNT_LIMIT,
                        application.getRequestedAmount().compareTo(product.getMinAmount()) >= 0 && application.getRequestedAmount().compareTo(product.getMaxAmount()) <= 0,
                        "Requested amount must be within product limit"),
                result(application, EligibilityRule.TENURE_LIMIT,
                        application.getRequestedTenure() >= product.getMinTenure() && application.getRequestedTenure() <= product.getMaxTenure(),
                        "Requested tenure must be within product limit"),
                result(application, EligibilityRule.DSR_LIMIT, calculation.getProjectedDsr().compareTo(product.getMaximumDsr()) <= 0, "Projected DSR must be within product maximum"),
                result(application, EligibilityRule.DUPLICATE_ACTIVE_APPLICATION, !hasDuplicate(application), "Customer cannot have another active application")
        );
    }

    public RiskLevel riskLevel(BigDecimal projectedDsr) {
        if (projectedDsr.compareTo(new BigDecimal("30.0000")) <= 0) {
            return RiskLevel.LOW;
        }
        if (projectedDsr.compareTo(new BigDecimal("40.0000")) <= 0) {
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.HIGH;
    }

    public List<EligibilityResultResponse> findByApplication(UUID applicationId) {
        return eligibilityResultRepository.findByLoanApplicationId(applicationId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private boolean hasDuplicate(LoanApplication application) {
        LoanDataProvider provider = loanDataProviderResolver.resolve();
        return provider.hasActiveApplication(application.getCustomer().getId());
    }

    private EligibilityResult result(LoanApplication application, EligibilityRule rule, boolean passed, String message) {
        EligibilityResult result = new EligibilityResult();
        result.setLoanApplication(application);
        result.setRuleName(rule);
        result.setPassed(passed);
        result.setMessage(message);
        return result;
    }

    private EligibilityResultResponse toResponse(EligibilityResult result) {
        return EligibilityResultResponse.builder()
                .id(result.getId())
                .loanApplicationId(result.getLoanApplication().getId())
                .ruleName(result.getRuleName())
                .passed(result.getPassed())
                .message(result.getMessage())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
