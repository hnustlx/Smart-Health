package com.smarthealth.service;

import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiRoutingServiceTest {

    @Mock
    private UserAiConfigMapper configMapper;

    @Mock
    private DeepSeekAiService deepSeekAiService;

    @Mock
    private OllamaAiService ollamaAiService;

    @InjectMocks
    private AiRoutingService aiRoutingService;

    @Test
    void chat_shouldUseCustomKey_whenConfigIsCUSTOM() {
        UserAiConfig config = new UserAiConfig();
        config.setProvider("CUSTOM");
        config.setApiKey("sk-custom");
        config.setApiUrl("https://custom.api/v1/chat");
        config.setModel("custom-model");
        when(configMapper.findByUserId(1L)).thenReturn(config);
        when(deepSeekAiService.chat("sk-custom", "https://custom.api/v1/chat", "custom-model",
                "system", "user")).thenReturn("custom response");

        String result = aiRoutingService.chat(1L, "USER", "system", "user");

        assertEquals("custom response", result);
        verify(deepSeekAiService).chat("sk-custom", "https://custom.api/v1/chat", "custom-model",
                "system", "user");
    }

    @Test
    void chat_shouldUseOllama_whenVIPAndConfigIsLOCAL() {
        UserAiConfig config = new UserAiConfig();
        config.setProvider("LOCAL");
        when(configMapper.findByUserId(1L)).thenReturn(config);
        when(ollamaAiService.chat("system", "user")).thenReturn("ollama response");

        String result = aiRoutingService.chat(1L, "VIP", "system", "user");

        assertEquals("ollama response", result);
        verify(ollamaAiService).chat("system", "user");
    }

    @Test
    void chat_shouldUseServerDeepSeek_whenNoConfig() {
        when(configMapper.findByUserId(1L)).thenReturn(null);
        when(deepSeekAiService.chat("system", "user")).thenReturn("default response");

        String result = aiRoutingService.chat(1L, "USER", "system", "user");

        assertEquals("default response", result);
        verify(deepSeekAiService).chat("system", "user");
    }
}
