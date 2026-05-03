package com.aegira.loan.loanapplication.service;

import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.calculation.service.LoanCalculationService;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.ForbiddenException;
import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.dto.LoanApplicationRequest;
import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.provider.LoanDataProvider;
import com.aegira.loan.loanapplication.provider.LoanDataProviderResolver;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.loanproduct.entity.LoanProduct;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanDataProviderResolver loanDataProviderResolver;
    private final SecurityUtil securityUtil;
    private final LoanCalculationService loanCalculationService;
    private final EligibilityService eligibilityService;
    private final AuditService auditService;

    @Transactional
    public LoanApplicationResponse create(LoanApplicationRequest request) {
        User agent = securityUtil.currentUser();
        LoanDataProvider provider = loanDataProviderResolver.resolve();
        Customer customer = provider.getCustomerById(request.getCustomerId());
        LoanProduct product = provider.getActiveLoanProductById(request.getLoanProductId());
        LoanApplication application = new LoanApplication();
        application.setApplicationNumber(nextNumber());
        application.setCustomer(customer);
        application.setAgent(agent);
        application.setLoanProduct(product);
        application.setRequestedAmount(request.getRequestedAmount());
        application.setRequestedTenure(request.getRequestedTenure());
        application.setLoanPurpose(request.getLoanPurpose());
        application.setStatus(ApplicationStatus.DRAFT);
        loanApplicationRepository.save(application);
        auditService.log("LOAN_APPLICATION", application.getId(), "CREATE", agent, null, application.getStatus().name(), null, customer.getId().toString());
        return toResponse(application);
    }

    public List<LoanApplicationResponse> findAllVisible() {
        User user = securityUtil.currentUser();
        List<LoanApplication> applications = user.getRole() == Role.AGENT
                ? loanApplicationRepository.findByAgentId(user.getId())
                : loanApplicationRepository.findByStatusNot(ApplicationStatus.DRAFT);
        return applications.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public LoanApplication getVisible(UUID id) {
        LoanApplication application = get(id);
        User user = securityUtil.currentUser();
        if (user.getRole() == Role.AGENT && !application.getAgent().getId().equals(user.getId())) {
            throw new ForbiddenException("Agent can only view own loan applications");
        }
        if (user.getRole() != Role.AGENT && application.getStatus() == ApplicationStatus.DRAFT) {
            throw new ForbiddenException("Only submitted applications are visible to this role");
        }
        return application;
    }

    public LoanApplicationResponse findById(UUID id) {
        return toResponse(getVisible(id));
    }

    @Transactional
    public LoanApplicationResponse update(UUID id, LoanApplicationRequest request) {
        LoanApplication application = getVisible(id);
        if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.REVISION_REQUESTED) {
            throw new BadRequestException("Only draft or revision requested applications can be updated");
        }
        LoanDataProvider provider = loanDataProviderResolver.resolve();
        application.setCustomer(provider.getCustomerById(request.getCustomerId()));
        application.setLoanProduct(provider.getActiveLoanProductById(request.getLoanProductId()));
        application.setRequestedAmount(request.getRequestedAmount());
        application.setRequestedTenure(request.getRequestedTenure());
        application.setLoanPurpose(request.getLoanPurpose());
        auditService.log("LOAN_APPLICATION", application.getId(), "UPDATE", securityUtil.currentUser(), null, application.getStatus().name(), null, application.getCustomer().getId().toString());
        return toResponse(application);
    }

    @Transactional
    public LoanApplicationResponse submit(UUID id) {
        LoanApplication application = getVisible(id);
        MDC.put("correlationId", application.getCustomer().getId().toString());
        try {
            if (application.getStatus() != ApplicationStatus.DRAFT && application.getStatus() != ApplicationStatus.REVISION_REQUESTED) {
                throw new BadRequestException("Only draft or revision requested applications can be submitted");
            }
            LoanDataProvider provider = loanDataProviderResolver.resolve();
            application.setCustomer(provider.getCustomerById(application.getCustomer().getId()));
            application.setLoanProduct(provider.getActiveLoanProductById(application.getLoanProduct().getId()));
            LoanCalculation calculation = loanCalculationService.calculateAndSave(application);
            List<EligibilityResult> results = eligibilityService.evaluateAndSave(application, calculation);
            application.setRiskLevel(eligibilityService.riskLevel(calculation.getProjectedDsr()));
            application.setStatus(ApplicationStatus.WAITING_RISK_REVIEW);
            application.setSubmittedAt(OffsetDateTime.now());
            auditService.log("LOAN_APPLICATION", application.getId(), "SUBMIT", securityUtil.currentUser(), "DRAFT",
                    application.getStatus().name(), "eligible=" + results.stream().allMatch(EligibilityResult::getPassed),
                    application.getCustomer().getId().toString());
            log.info("loan application submitted applicationId={} status={} riskLevel={}", application.getId(), application.getStatus(), application.getRiskLevel());
            return toResponse(application);
        } finally {
            MDC.remove("correlationId");
        }
    }

    public LoanApplication get(UUID id) {
        return loanApplicationRepository.findById(id).orElseThrow(() -> new NotFoundException("Loan application not found"));
    }

    public LoanApplicationResponse toResponse(LoanApplication application) {
        return LoanApplicationResponse.builder()
                .id(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .customerId(application.getCustomer().getId())
                .agentId(application.getAgent().getId())
                .loanProductId(application.getLoanProduct().getId())
                .requestedAmount(application.getRequestedAmount())
                .requestedTenure(application.getRequestedTenure())
                .loanPurpose(application.getLoanPurpose())
                .status(application.getStatus())
                .riskLevel(application.getRiskLevel())
                .submittedAt(application.getSubmittedAt())
                .createdAt(application.getCreatedAt())
                .updatedAt(application.getUpdatedAt())
                .version(application.getVersion())
                .build();
    }

    private String nextNumber() {
        return "LA-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(OffsetDateTime.now());
    }
}
