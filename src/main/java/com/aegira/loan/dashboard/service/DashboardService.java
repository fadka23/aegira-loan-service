package com.aegira.loan.dashboard.service;

import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.dashboard.dto.AgentDashboardSummaryResponse;
import com.aegira.loan.dashboard.dto.HoDashboardSummaryResponse;
import com.aegira.loan.dashboard.dto.RiskDashboardSummaryResponse;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.entity.RiskLevel;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final LoanApplicationRepository loanApplicationRepository;
    private final SecurityUtil securityUtil;

    public AgentDashboardSummaryResponse agentSummary() {
        User agent = securityUtil.currentUser();
        long approved = loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.HO_APPROVED);
        long rejected = loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.RISK_REJECTED)
                + loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.HO_REJECTED);
        return new AgentDashboardSummaryResponse(
                loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.DRAFT),
                loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.WAITING_RISK_REVIEW),
                approved,
                rejected,
                loanApplicationRepository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.REVISION_REQUESTED)
        );
    }

    public RiskDashboardSummaryResponse riskSummary() {
        OffsetDateTime from = startOfToday();
        OffsetDateTime to = from.plusDays(1);
        return new RiskDashboardSummaryResponse(
                loanApplicationRepository.countByStatus(ApplicationStatus.WAITING_RISK_REVIEW),
                loanApplicationRepository.countByStatusAndRiskLevel(ApplicationStatus.WAITING_RISK_REVIEW, RiskLevel.MEDIUM),
                loanApplicationRepository.countByStatusAndRiskLevel(ApplicationStatus.WAITING_RISK_REVIEW, RiskLevel.HIGH),
                loanApplicationRepository.countByStatusAndUpdatedAtBetween(ApplicationStatus.WAITING_HO_APPROVAL, from, to)
                        + loanApplicationRepository.countByStatusAndUpdatedAtBetween(ApplicationStatus.HO_APPROVED, from, to),
                loanApplicationRepository.countByStatusAndUpdatedAtBetween(ApplicationStatus.RISK_REJECTED, from, to)
        );
    }

    public HoDashboardSummaryResponse hoSummary() {
        OffsetDateTime from = startOfToday();
        OffsetDateTime to = from.plusDays(1);
        return new HoDashboardSummaryResponse(
                loanApplicationRepository.countByStatus(ApplicationStatus.WAITING_HO_APPROVAL),
                loanApplicationRepository.countByStatusAndUpdatedAtBetween(ApplicationStatus.HO_APPROVED, from, to),
                loanApplicationRepository.countByStatusAndUpdatedAtBetween(ApplicationStatus.HO_REJECTED, from, to)
        );
    }

    private OffsetDateTime startOfToday() {
        ZoneOffset offset = ZoneId.systemDefault().getRules().getOffset(java.time.Instant.now());
        return LocalDate.now().atStartOfDay().atOffset(offset);
    }
}
