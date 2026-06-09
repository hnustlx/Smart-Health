package com.smarthealth.service;

import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import com.smarthealth.util.EncryptionUtil;
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

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private ClaudeAiService claudeAiService;

    @InjectMocks
    private AiRoutingService aiRoutingService;

    @Test
    void chat_shouldUseCustomKey_whenConfigIsCUSTOM() {
        UserAiConfig config = new UserAiConfig();
        config.setProvider("CUSTOM");
        config.setApiKey("encrypted:sk-custom");
        config.setApiUrl("https://custom.api/v1/chat");
        config.setModel("custom-model");
        when(configMapper.findByUserId(1L)).thenReturn(config);
        when(encryptionUtil.decrypt("encrypted:sk-custom")).thenReturn("sk-custom");
        when(deepSeekAiService.chat("sk-custom", "https://custom.api/v1/chat", "custom-model",
                "system", "user")).thenReturn("custom response");

        String result = aiRoutingService.chat(1L, "USER", "system", "user");

        assertEquals("custom response", result);
        verify(encryptionUtil).decrypt("encrypted:sk-custom");
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
    void chat_shouldUseClaude_whenConfigIsClaude() {
        UserAiConfig config = new UserAiConfig();
        config.setProvider("CUSTOM");
        config.setCustomProvider("claude");
        config.setApiKey("encrypted:sk-claude-key");
        config.setApiUrl("https://api.anthropic.com/v1/messages");
        config.setModel("claude-3-opus-20240229");
        when(configMapper.findByUserId(1L)).thenReturn(config);
        when(encryptionUtil.decrypt("encrypted:sk-claude-key")).thenReturn("sk-claude-key");
        when(claudeAiService.chat("sk-claude-key", "https://api.anthropic.com/v1/messages",
                "claude-3-opus-20240229", "system", "user")).thenReturn("claude response");

        String result = aiRoutingService.chat(1L, "USER", "system", "user");

        assertEquals("claude response", result);
        verify(encryptionUtil).decrypt("encrypted:sk-claude-key");
        verify(claudeAiService).chat("sk-claude-key", "https://api.anthropic.com/v1/messages",
                "claude-3-opus-20240229", "system", "user");
        verifyNoInteractions(deepSeekAiService);
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
