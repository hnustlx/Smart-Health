package com.smarthealth.service;

import com.smarthealth.dto.response.AiConfigResponse;
import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAiConfigServiceTest {

    @Mock
    private UserAiConfigMapper configMapper;

    @InjectMocks
    private UserAiConfigService userAiConfigService;

    @Test
    void maskApiKey_shouldMaskCorrectly() {
        assertEquals("sk-abc****7890", UserAiConfigService.maskApiKey("sk-abcd12347890"));
    }

    @Test
    void maskApiKey_shouldReturnFourStars_whenTooShort() {
        assertEquals("****", UserAiConfigService.maskApiKey("ab"));
    }

    @Test
    void maskApiKey_shouldHandleNull() {
        assertEquals("****", UserAiConfigService.maskApiKey(null));
    }

    @Test
    void getConfig_shouldReturnDefault_whenNoConfig() {
        when(configMapper.findByUserId(1L)).thenReturn(null);

        AiConfigResponse response = userAiConfigService.getConfig(1L);

        assertEquals("DEFAULT", response.getProvider());
        assertNull(response.getApiKey());
    }

    @Test
    void getConfig_shouldMaskApiKey_whenConfigExists() {
        UserAiConfig config = new UserAiConfig();
        config.setProvider("CUSTOM");
        config.setApiKey("sk-abcdefghijk12345");
        config.setApiUrl("https://api.deepseek.com");
        config.setModel("deepseek-chat");
        when(configMapper.findByUserId(1L)).thenReturn(config);

        AiConfigResponse response = userAiConfigService.getConfig(1L);

        assertEquals("CUSTOM", response.getProvider());
        assertEquals("sk-abc****2345", response.getApiKey());
    }

    @Test
    void deleteConfig_shouldDeleteByUserId() {
        userAiConfigService.deleteConfig(1L);
        verify(configMapper).deleteByUserId(1L);
    }
}
