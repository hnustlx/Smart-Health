package com.smarthealth.controller.admin;

import com.smarthealth.config.SecurityConfig;
import com.smarthealth.dto.response.AdminDashboardResponse;
import com.smarthealth.dto.response.DashboardRecentUserResponse;
import com.smarthealth.dto.response.DashboardTopKnowledgeResponse;
import com.smarthealth.security.JwtAuthenticationFilter;
import com.smarthealth.security.JwtTokenProvider;
import com.smarthealth.security.UserPrincipal;
import com.smarthealth.service.AdminDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class AdminDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminDashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = new UserPrincipal(1L, "admin", "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void getDashboard_shouldReturn200() throws Exception {
        AdminDashboardResponse response = new AdminDashboardResponse(
                new AdminDashboardResponse.UserStats(10L, 2L, 5L, 8L, 1L, 3L, 1L),
                new AdminDashboardResponse.PlanStats(6L, 20L, 4L, 2L),
                new AdminDashboardResponse.KnowledgeStats(12L, 10L, 2L, 9L, 40L),
                List.of(new DashboardTopKnowledgeResponse(
                        "knowledge_1", "减脂原则", "减脂", 9L,
                        LocalDateTime.of(2026, 6, 9, 10, 0))),
                List.of(new DashboardRecentUserResponse(
                        2L, "testuser", "VIP", 1, true,
                        LocalDateTime.of(2026, 6, 9, 10, 5))));

        when(dashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/v1/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.users.total").value(10))
                .andExpect(jsonPath("$.data.plans.generatedToday").value(6))
                .andExpect(jsonPath("$.data.knowledge.referencedToday").value(9))
                .andExpect(jsonPath("$.data.topKnowledge[0].title").value("减脂原则"))
                .andExpect(jsonPath("$.data.recentActiveUsers[0].online").value(true));
    }
}
