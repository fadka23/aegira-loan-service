package com.aegira.loan.loanapplication.dto;

import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LoanApplicationDetailResponse {
    private UUID id;
    private String applicationNumber;
    private ApplicationStatus status;
    private RiskLevel riskLevel;
    private CustomerDetail customer;
    private LoanProductDetail loanProduct;
    private LoanDetail loan;
    private CalculationDetail calculation;
    private List<EligibilityDetail> eligibilityResults;
    private List<ApprovalHistoryDetail> approvalHistories;

    @Data
    @Builder
    public static class CustomerDetail {
        private UUID id;
        private String nik;
        private String name;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private BigDecimal monthlyIncome;
        private BigDecimal monthlyExpense;
        private BigDecimal existingInstallment;
    }

    @Data
    @Builder
    public static class LoanProductDetail {
        private UUID id;
        private String name;
        private BigDecimal annualInterestRate;
        private BigDecimal maximumDsr;
    }

    @Data
    @Builder
    public static class LoanDetail {
        private BigDecimal requestedAmount;
        private Integer requestedTenure;
        private String loanPurpose;
    }

    @Data
    @Builder
    public static class CalculationDetail {
        private BigDecimal totalInterest;
        private BigDecimal totalPayment;
        private BigDecimal monthlyInstallment;
        private BigDecimal currentDsr;
        private BigDecimal projectedDsr;
        private Boolean eligible;
    }

    @Data
    @Builder
    public static class EligibilityDetail {
        private String ruleCode;
        private String ruleName;
        private Boolean passed;
        private String message;
    }

    @Data
    @Builder
    public static class ApprovalHistoryDetail {
        private String approverName;
        private String approverRole;
        private String decision;
        private String notes;
        private OffsetDateTime createdAt;
    }
}
