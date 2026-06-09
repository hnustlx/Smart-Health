package com.smarthealth.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthealth.common.RateLimitService;
import com.smarthealth.dto.response.AdminAiConfigResponse;
import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import com.smarthealth.security.JwtAuthenticationFilter;
import com.smarthealth.security.JwtTokenProvider;
import com.smarthealth.security.UserPrincipal;
import com.smarthealth.service.UserAiConfigService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminAiConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAiConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAiConfigMapper configMapper;

    @MockBean
    private UserAiConfigService userAiConfigService;

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
    void listConfigs_shouldReturnPaginatedResults() throws Exception {
        UserAiConfig config = new UserAiConfig();
        config.setUserId(2L);
        config.setUsername("testuser");
        config.setProvider("CUSTOM");
        config.setCustomProvider("deepseek");
        config.setApiKey("encrypted:sk-test");
        config.setApiUrl("https://api.deepseek.com");
        config.setModel("deepseek-chat");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        when(configMapper.findAll(isNull(), eq(0), eq(10))).thenReturn(List.of(config));
        when(configMapper.countAll(isNull())).thenReturn(1L);

        mockMvc.perform(get("/api/v1/admin/ai-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("testuser"))
                .andExpect(jsonPath("$.data.records[0].apiKey").value("****"))
                .andExpect(jsonPath("$.data.records[0].customProvider").value("deepseek"));
    }

    @Test
    void listConfigs_shouldSearchByKeyword() throws Exception {
        when(configMapper.findAll(eq("test"), eq(0), eq(10))).thenReturn(List.of());
        when(configMapper.countAll(eq("test"))).thenReturn(0L);

        mockMvc.perform(get("/api/v1/admin/ai-configs?keyword=test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void getConfig_shouldReturnUserConfig() throws Exception {
        UserAiConfig config = new UserAiConfig();
        config.setUserId(2L);
        config.setUsername("testuser");
        config.setProvider("CUSTOM");
        config.setCustomProvider("claude");
        config.setApiKey("encrypted:sk-claude");
        config.setApiUrl("https://api.anthropic.com");
        config.setModel("claude-3-haiku-20240307");
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());

        when(configMapper.findByUserIdWithUsername(2L)).thenReturn(config);

        mockMvc.perform(get("/api/v1/admin/ai-configs/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.apiKey").value("****"))
                .andExpect(jsonPath("$.data.customProvider").value("claude"))
                .andExpect(jsonPath("$.data.provider").value("CUSTOM"));
    }

    @Test
    void getConfig_shouldReturn404_whenNotFound() throws Exception {
        when(configMapper.findByUserIdWithUsername(99L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/admin/ai-configs/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void resetConfig_shouldDelete() throws Exception {
        UserAiConfig config = new UserAiConfig();
        config.setUserId(2L);
        when(configMapper.findByUserId(2L)).thenReturn(config);

        mockMvc.perform(delete("/api/v1/admin/ai-configs/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void resetConfig_shouldReturn404_whenNotFound() throws Exception {
        when(configMapper.findByUserId(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/v1/admin/ai-configs/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }
}
