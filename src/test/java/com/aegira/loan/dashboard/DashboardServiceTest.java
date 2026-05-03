package com.aegira.loan.dashboard;

import com.aegira.loan.common.security.SecurityUtil;
import com.aegira.loan.dashboard.dto.AgentDashboardSummaryResponse;
import com.aegira.loan.dashboard.service.DashboardService;
import com.aegira.loan.loanapplication.entity.ApplicationStatus;
import com.aegira.loan.loanapplication.repository.LoanApplicationRepository;
import com.aegira.loan.user.entity.Role;
import com.aegira.loan.user.entity.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {
    @Test
    void dashboardSummaryReturnsCorrectCounts() {
        LoanApplicationRepository repository = mock(LoanApplicationRepository.class);
        SecurityUtil securityUtil = mock(SecurityUtil.class);
        DashboardService service = new DashboardService(repository, securityUtil);
        User agent = new User();
        agent.setId(UUID.randomUUID());
        agent.setRole(Role.AGENT);
        when(securityUtil.currentUser()).thenReturn(agent);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.DRAFT)).thenReturn(5L);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.WAITING_RISK_REVIEW)).thenReturn(3L);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.HO_APPROVED)).thenReturn(10L);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.RISK_REJECTED)).thenReturn(1L);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.HO_REJECTED)).thenReturn(1L);
        when(repository.countByAgentIdAndStatus(agent.getId(), ApplicationStatus.REVISION_REQUESTED)).thenReturn(1L);

        AgentDashboardSummaryResponse response = service.agentSummary();

        assertEquals(5L, response.getTotalDraft());
        assertEquals(3L, response.getTotalWaitingRiskReview());
        assertEquals(10L, response.getTotalApproved());
        assertEquals(2L, response.getTotalRejected());
        assertEquals(1L, response.getTotalRevisionRequested());
    }
}
