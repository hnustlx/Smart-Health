package com.smarthealth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthealth.common.RateLimitService;
import com.smarthealth.dto.request.AiConfigRequest;
import com.smarthealth.dto.response.AiConfigResponse;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AiConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAiConfigService userAiConfigService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        UserPrincipal principal = new UserPrincipal(1L, "testuser", "USER");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    @Test
    void getConfig_shouldReturnConfig() throws Exception {
        when(userAiConfigService.getConfig(1L))
                .thenReturn(new AiConfigResponse("DEFAULT", null, null, null, null));

        mockMvc.perform(get("/api/v1/ai-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.provider").value("DEFAULT"));
    }

    @Test
    void saveConfig_shouldSaveAndReturn() throws Exception {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("CUSTOM");
        request.setCustomProvider("deepseek");
        request.setApiKey("sk-test12345678");
        request.setApiUrl("https://api.deepseek.com/v1/chat/completions");
        request.setModel("deepseek-chat");

        when(userAiConfigService.saveConfig(eq(1L), eq("USER"), any()))
                .thenReturn(new AiConfigResponse("CUSTOM", "deepseek", "sk-te****5678",
                        "https://api.deepseek.com/v1/chat/completions", "deepseek-chat"));

        mockMvc.perform(post("/api/v1/ai-config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.provider").value("CUSTOM"));
    }

    @Test
    void deleteConfig_shouldReset() throws Exception {
        mockMvc.perform(delete("/api/v1/ai-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
