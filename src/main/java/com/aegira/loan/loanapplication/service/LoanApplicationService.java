package com.aegira.loan.loanapplication.service;

import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.approval.entity.ApprovalHistory;
import com.aegira.loan.approval.repository.ApprovalHistoryRepository;
import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.calculation.repository.LoanCalculationRepository;
import com.aegira.loan.calculation.service.LoanCalculationService;
import com.aegira.loan.common.dto.PageResponse;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.exception.ForbiddenException;
import com.aegira.loan.common.exception.NotFoundException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.eligibility.entity.EligibilityResult;
import com.aegira.loan.eligibility.repository.EligibilityResultRepository;
import com.aegira.loan.eligibility.service.EligibilityService;
import com.aegira.loan.loanapplication.dto.LoanApplicationDetailResponse;
import com.aegira.loan.loanapplication.dto.LoanApplicationListFilter;
import com.aegira.loan.loanapplication.dto.LoanApplicationListItemResponse;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
    private final LoanCalculationRepository loanCalculationRepository;
    private final EligibilityService eligibilityService;
    private final EligibilityResultRepository eligibilityResultRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
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

    public PageResponse<LoanApplicationListItemResponse> findListForUi(LoanApplicationListFilter filter) {
        User user = securityUtil.currentUser();
        List<LoanApplication> applications = user.getRole() == Role.AGENT
                ? loanApplicationRepository.findByAgentId(user.getId())
                : loanApplicationRepository.findAll();
        List<LoanApplicationListItemResponse> filtered = applications.stream()
                .filter(application -> matchesFilter(application, filter))
                .sorted(Comparator.comparing(LoanApplication::getCreatedAt).reversed())
                .map(this::toListItem)
                .collect(Collectors.toList());
        return page(filtered, filter.getPage(), filter.getSize());
    }

    public LoanApplicationDetailResponse detail(UUID id) {
        LoanApplication application = getVisible(id);
        Optional<LoanCalculation> calculation = loanCalculationRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(id);
        List<EligibilityResult> eligibilityResults = eligibilityResultRepository.findByLoanApplicationId(id);
        List<ApprovalHistory> histories = approvalHistoryRepository.findByLoanApplicationIdOrderByCreatedAtAsc(id);
        Customer customer = application.getCustomer();
        LoanProduct product = application.getLoanProduct();
        return LoanApplicationDetailResponse.builder()
                .id(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .status(application.getStatus())
                .riskLevel(application.getRiskLevel())
                .customer(LoanApplicationDetailResponse.CustomerDetail.builder()
                        .id(customer.getId())
                        .nik(customer.getNik())
                        .name(customer.getName())
                        .phoneNumber(customer.getPhoneNumber())
                        .dateOfBirth(customer.getDateOfBirth())
                        .monthlyIncome(customer.getMonthlyIncome())
                        .monthlyExpense(customer.getMonthlyExpense())
                        .existingInstallment(customer.getExistingInstallment())
                        .build())
                .loanProduct(LoanApplicationDetailResponse.LoanProductDetail.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .annualInterestRate(product.getAnnualInterestRate())
                        .maximumDsr(product.getMaximumDsr())
                        .build())
                .loan(LoanApplicationDetailResponse.LoanDetail.builder()
                        .requestedAmount(application.getRequestedAmount())
                        .requestedTenure(application.getRequestedTenure())
                        .loanPurpose(application.getLoanPurpose())
                        .build())
                .calculation(calculation.map(this::toCalculationDetail).orElse(null))
                .eligibilityResults(eligibilityResults.stream().map(this::toEligibilityDetail).collect(Collectors.toList()))
                .approvalHistories(histories.stream().map(this::toApprovalHistoryDetail).collect(Collectors.toList()))
                .build();
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
        MDC.put("business_correlation_id", application.getCustomer().getId().toString());
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
            log.info("event_name=loan_application_submitted application_id={} status={} risk_level={}", application.getId(), application.getStatus(), application.getRiskLevel());
            return toResponse(application);
        } finally {
            MDC.remove("business_correlation_id");
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

    private boolean matchesFilter(LoanApplication application, LoanApplicationListFilter filter) {
        if (filter.getStatus() != null && application.getStatus() != filter.getStatus()) {
            return false;
        }
        if (filter.getRiskLevel() != null && application.getRiskLevel() != filter.getRiskLevel()) {
            return false;
        }
        if (hasText(filter.getCustomerName()) && !contains(application.getCustomer().getName(), filter.getCustomerName())) {
            return false;
        }
        if (hasText(filter.getApplicationNumber()) && !contains(application.getApplicationNumber(), filter.getApplicationNumber())) {
            return false;
        }
        if (filter.getFromDate() != null && application.getCreatedAt().toLocalDate().isBefore(filter.getFromDate())) {
            return false;
        }
        if (filter.getToDate() != null && application.getCreatedAt().toLocalDate().isAfter(filter.getToDate())) {
            return false;
        }
        return true;
    }

    private LoanApplicationListItemResponse toListItem(LoanApplication application) {
        Optional<LoanCalculation> calculation = loanCalculationRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(application.getId());
        return LoanApplicationListItemResponse.builder()
                .id(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .customerName(application.getCustomer().getName())
                .customerNik(application.getCustomer().getNik())
                .requestedAmount(application.getRequestedAmount())
                .requestedTenure(application.getRequestedTenure())
                .monthlyInstallment(calculation.map(LoanCalculation::getMonthlyInstallment).orElse(null))
                .projectedDsr(calculation.map(LoanCalculation::getProjectedDsr).orElse(null))
                .status(application.getStatus())
                .riskLevel(application.getRiskLevel())
                .createdAt(application.getCreatedAt())
                .build();
    }

    private LoanApplicationDetailResponse.CalculationDetail toCalculationDetail(LoanCalculation calculation) {
        return LoanApplicationDetailResponse.CalculationDetail.builder()
                .totalInterest(calculation.getTotalInterest())
                .totalPayment(calculation.getTotalPayment())
                .monthlyInstallment(calculation.getMonthlyInstallment())
                .currentDsr(calculation.getCurrentDsr())
                .projectedDsr(calculation.getProjectedDsr())
                .eligible(calculation.getEligible())
                .build();
    }

    private LoanApplicationDetailResponse.EligibilityDetail toEligibilityDetail(EligibilityResult result) {
        return LoanApplicationDetailResponse.EligibilityDetail.builder()
                .ruleCode(result.getRuleName().name())
                .ruleName(toTitle(result.getRuleName().name()))
                .passed(result.getPassed())
                .message(result.getMessage())
                .build();
    }

    private LoanApplicationDetailResponse.ApprovalHistoryDetail toApprovalHistoryDetail(ApprovalHistory history) {
        return LoanApplicationDetailResponse.ApprovalHistoryDetail.builder()
                .approverName(history.getPerformedBy().getEmail())
                .approverRole(history.getPerformedBy().getRole().name())
                .decision(toUiDecision(history.getDecision().name()))
                .notes(history.getNotes())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private <T> PageResponse<T> page(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        List<T> content = new ArrayList<T>(items.subList(from, to));
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);
        return PageResponse.<T>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(items.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ENGLISH).contains(keyword.toLowerCase(Locale.ENGLISH));
    }

    private String toTitle(String value) {
        String[] parts = value.toLowerCase(Locale.ENGLISH).split("_");
        List<String> words = new ArrayList<String>();
        for (String part : parts) {
            if (part.length() > 0) {
                words.add(part.substring(0, 1).toUpperCase(Locale.ENGLISH) + part.substring(1));
            }
        }
        return String.join(" ", words);
    }

    private String toUiDecision(String decision) {
        if (decision.endsWith("_APPROVE")) {
            return "APPROVED";
        }
        if (decision.endsWith("_REJECT")) {
            return "REJECTED";
        }
        if (decision.endsWith("_REQUEST_REVISION")) {
            return "REVISION_REQUESTED";
        }
        return decision;
    }

    private String nextNumber() {
        return "LA-" + DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS").format(OffsetDateTime.now());
    }
}
