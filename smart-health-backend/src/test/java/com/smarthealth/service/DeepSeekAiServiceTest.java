package com.smarthealth.service;

import com.smarthealth.config.DeepSeekConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeepSeekAiServiceTest {

    @Mock
    private DeepSeekConfig deepSeekConfig;

    @Mock
    private RestTemplate deepSeekRestTemplate;

    @InjectMocks
    private DeepSeekAiService deepSeekAiService;

    @BeforeEach
    void setUp() {
        when(deepSeekConfig.getUrl()).thenReturn("https://api.deepseek.com/v1/chat/completions");
        when(deepSeekConfig.getApiKey()).thenReturn("test-api-key");
        when(deepSeekConfig.getModel()).thenReturn("deepseek-chat");
    }

    @Test
    void chat_shouldReturnContent() {
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "{\"dietPlan\":[]}"))
                )
        );
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = deepSeekAiService.chat("system prompt", "user prompt");

        assertEquals("{\"dietPlan\":[]}", result);
    }

    @Test
    void chat_shouldNotRequestJsonFormat_whenPromptDoesNotRequireJson() {
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "健康问答回复"))
                )
        );
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = deepSeekAiService.chat("system prompt", "user prompt");

        assertEquals("健康问答回复", result);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(deepSeekRestTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
        assertFalse(((Map<?, ?>) captor.getValue().getBody()).containsKey("response_format"));
    }

    @Test
    void chat_shouldRequestJsonFormat_whenPromptRequiresJson() {
        Map<String, Object> responseBody = Map.of(
                "choices", List.of(
                        Map.of("message", Map.of("content", "{\"dietPlan\":[]}"))
                )
        );
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = deepSeekAiService.chat("只返回 JSON", "user prompt");

        assertEquals("{\"dietPlan\":[]}", result);
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(deepSeekRestTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));
        assertTrue(((Map<?, ?>) captor.getValue().getBody()).containsKey("response_format"));
    }

    @Test
    void chat_shouldThrow_whenEmptyChoices() {
        Map<String, Object> responseBody = Map.of("choices", List.of());
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> deepSeekAiService.chat("system", "user"));
    }

    @Test
    void chat_shouldThrow_whenNoChoicesKey() {
        Map<String, Object> responseBody = Map.of("error", "invalid");
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> deepSeekAiService.chat("system", "user"));
    }

    @Test
    void chat_shouldThrow_whenApiFails() {
        when(deepSeekRestTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection timeout"));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> deepSeekAiService.chat("system", "user"));
    }
}
