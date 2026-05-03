package com.aegira.loan.approval;

import com.aegira.loan.approval.dto.ApprovalRequest;
import com.aegira.loan.approval.entity.ApprovalHistory;
import com.aegira.loan.approval.repository.ApprovalHistoryRepository;
import com.aegira.loan.approval.service.ApprovalService;
import com.aegira.loan.audit.service.AuditService;
import com.aegira.loan.common.exception.BadRequestException;
import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.customer.entity.Customer;
import com.aegira.loan.loanapplication.dto.LoanApplicationResponse;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.LoanApplication;
import com.aegira.loan.loanapplication.service.LoanApplicationService;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovalServiceTest {
    private LoanApplicationService loanApplicationService;
    private ApprovalHistoryRepository approvalHistoryRepository;
    private AuditService auditService;
    private SecurityUtil securityUtil;
    private ApprovalService service;

    @BeforeEach
    void setUp() {
        loanApplicationService = mock(LoanApplicationService.class);
        approvalHistoryRepository = mock(ApprovalHistoryRepository.class);
        auditService = mock(AuditService.class);
        securityUtil = mock(SecurityUtil.class);
        service = new ApprovalService(loanApplicationService, approvalHistoryRepository, auditService, securityUtil);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.RISK);
        when(securityUtil.currentUser()).thenReturn(user);
        when(loanApplicationService.toResponse(any(LoanApplication.class))).thenAnswer(invocation -> {
            LoanApplication app = invocation.getArgument(0);
            return LoanApplicationResponse.builder().id(app.getId()).status(app.getStatus()).build();
        });
    }

    @Test
    void riskApproveValidStatus() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        ApprovalRequest request = new ApprovalRequest();
        request.setApprovedAmount(new BigDecimal("50000000.00"));
        assertEquals(ApplicationStatus.HO_APPROVED, service.riskApprove(application.getId(), request).getStatus());
    }

    @Test
    void riskRejectValidStatus() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        ApprovalRequest request = new ApprovalRequest();
        request.setNotes("Not eligible");
        assertEquals(ApplicationStatus.RISK_REJECTED, service.riskReject(application.getId(), request).getStatus());
    }

    @Test
    void riskCannotApproveDraft() {
        LoanApplication application = application(ApplicationStatus.DRAFT);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        assertThrows(BadRequestException.class, () -> service.riskApprove(application.getId(), new ApprovalRequest()));
    }

    @Test
    void hoApproveValidStatus() {
        LoanApplication application = application(ApplicationStatus.WAITING_HO_APPROVAL);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        assertEquals(ApplicationStatus.HO_APPROVED, service.hoApprove(application.getId(), new ApprovalRequest()).getStatus());
    }

    @Test
    void hoCannotApproveWaitingRiskReview() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        assertThrows(BadRequestException.class, () -> service.hoApprove(application.getId(), new ApprovalRequest()));
    }

    @Test
    void approvalCreatesApprovalHistory() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        service.riskApprove(application.getId(), new ApprovalRequest());
        verify(approvalHistoryRepository).save(any(ApprovalHistory.class));
    }

    @Test
    void approvalCreatesAuditLog() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        service.riskApprove(application.getId(), new ApprovalRequest());
        verify(auditService).log(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void approvalHistoryUsesCorrelationId() {
        LoanApplication application = application(ApplicationStatus.WAITING_RISK_REVIEW);
        when(loanApplicationService.get(application.getId())).thenReturn(application);
        service.riskApprove(application.getId(), new ApprovalRequest());
        ArgumentCaptor<ApprovalHistory> captor = ArgumentCaptor.forClass(ApprovalHistory.class);
        verify(approvalHistoryRepository).save(captor.capture());
        assertEquals(application.getCustomer().getId().toString(), captor.getValue().getCorrelationId());
    }

    private LoanApplication application(ApplicationStatus status) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        LoanApplication application = new LoanApplication();
        application.setId(UUID.randomUUID());
        application.setCustomer(customer);
        application.setStatus(status);
        application.setRequestedAmount(new BigDecimal("25000000.00"));
        return application;
    }
}
