package com.smarthealth.service;

import com.smarthealth.config.OllamaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaAiServiceTest {

    @Mock
    private OllamaConfig ollamaConfig;

    @Mock
    private RestTemplate ollamaRestTemplate;

    @InjectMocks
    private OllamaAiService ollamaAiService;

    @BeforeEach
    void setUp() {
        when(ollamaConfig.getUrl()).thenReturn("http://localhost:11434");
        when(ollamaConfig.getModel()).thenReturn("qwen2.5-7b-gguf:latest");
    }

    @Test
    void chat_shouldReturnContent() {
        Map<String, Object> responseBody = Map.of(
                "message", Map.of("role", "assistant", "content", "{\"dietPlan\":[]}")
        );
        when(ollamaRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = ollamaAiService.chat("system prompt", "user prompt");

        assertEquals("{\"dietPlan\":[]}", result);
    }

    @Test
    void chat_shouldThrow_whenNoMessageKey() {
        Map<String, Object> responseBody = Map.of("error", "invalid");
        when(ollamaRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> ollamaAiService.chat("system", "user"));
    }

    @Test
    void chat_shouldThrow_whenApiFails() {
        when(ollamaRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> ollamaAiService.chat("system", "user"));
    }
}
