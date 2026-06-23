package com.aegira.loan.approval.service;

import com.aegira.loan.approval.dto.ApprovalTaskFilter;
import com.aegira.loan.approval.dto.ApprovalTaskItemResponse;
import com.aegira.loan.calculation.entity.LoanCalculation;
import com.aegira.loan.calculation.repository.LoanCalculationRepository;
import com.aegira.loan.common.dto.PageResponse;
import com.aegira.loan.common.exception.ForbiddenException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalTaskService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanCalculationRepository loanCalculationRepository;
    private final SecurityUtil securityUtil;

    public PageResponse<ApprovalTaskItemResponse> tasks(ApprovalTaskFilter filter) {
        User user = securityUtil.currentUser();
        ApplicationStatus status;
        if (user.getRole() == Role.RISK) {
            status = ApplicationStatus.WAITING_RISK_REVIEW;
        } else if (user.getRole() == Role.HO) {
            status = ApplicationStatus.WAITING_HO_APPROVAL;
        } else {
            log.warn("event_name=access_denied user_id={} role={} error_code=FORBIDDEN", user.getId(), user.getRole());
            throw new ForbiddenException("Only RISK or HO can view approval tasks");
        }
        List<ApprovalTaskItemResponse> items = loanApplicationRepository.findByStatus(status).stream()
                .sorted(Comparator.comparing(LoanApplication::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toItem)
                .collect(Collectors.toList());
        return page(items, filter.getPage(), filter.getSize());
    }

    private ApprovalTaskItemResponse toItem(LoanApplication application) {
        Optional<LoanCalculation> calculation = loanCalculationRepository.findTopByLoanApplicationIdOrderByCreatedAtDesc(application.getId());
        return ApprovalTaskItemResponse.builder()
                .id(application.getId())
                .applicationNumber(application.getApplicationNumber())
                .customerName(application.getCustomer().getName())
                .requestedAmount(application.getRequestedAmount())
                .projectedDsr(calculation.map(LoanCalculation::getProjectedDsr).orElse(null))
                .riskLevel(application.getRiskLevel())
                .status(application.getStatus())
                .submittedAt(application.getSubmittedAt())
                .build();
    }

    private <T> PageResponse<T> page(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : size;
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);
        return PageResponse.<T>builder()
                .content(new ArrayList<T>(items.subList(from, to)))
                .page(safePage)
                .size(safeSize)
                .totalElements(items.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
    }
}
