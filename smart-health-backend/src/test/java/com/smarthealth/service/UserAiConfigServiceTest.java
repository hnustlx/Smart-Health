package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.dto.request.AiConfigRequest;
import com.smarthealth.dto.response.AiConfigResponse;
import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import com.smarthealth.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAiConfigServiceTest {

    @Mock
    private UserAiConfigMapper configMapper;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private RestTemplate restTemplate;

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
        config.setApiKey("encrypted:sk-abcdefghijk12345");
        config.setApiUrl("https://api.deepseek.com");
        config.setModel("deepseek-chat");
        when(configMapper.findByUserId(1L)).thenReturn(config);
        when(encryptionUtil.decrypt("encrypted:sk-abcdefghijk12345")).thenReturn("sk-abcdefghijk12345");

        AiConfigResponse response = userAiConfigService.getConfig(1L);

        assertEquals("CUSTOM", response.getProvider());
        assertEquals("sk-abc****2345", response.getApiKey());
    }

    @Test
    void deleteConfig_shouldDeleteByUserId() {
        userAiConfigService.deleteConfig(1L);
        verify(configMapper).deleteByUserId(1L);
    }

    @Test
    void saveConfig_shouldThrow_whenNonVipSavesLocal() {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("LOCAL");

        assertThrows(BusinessException.class,
                () -> userAiConfigService.saveConfig(1L, "USER", request));
    }

    @Test
    void saveConfig_shouldSucceed_whenVipSavesLocal() {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("LOCAL");

        when(configMapper.findByUserId(1L)).thenReturn(null);

        userAiConfigService.saveConfig(1L, "VIP", request);

        verify(configMapper).insert(argThat(config ->
                "LOCAL".equals(config.getProvider())
        ));
    }

    @Test
    void saveConfig_shouldEncryptApiKey_whenCustom() {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("CUSTOM");
        request.setApiKey("sk-test-key-12345");
        request.setApiUrl("https://api.deepseek.com/v1/chat/completions");
        request.setModel("deepseek-chat");

        when(encryptionUtil.encrypt("sk-test-key-12345")).thenReturn("encrypted:sk-test-key-12345");
        when(configMapper.findByUserId(1L)).thenReturn(null);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("choices", List.of(Map.of("message", Map.of())))));

        userAiConfigService.saveConfig(1L, "VIP", request);

        verify(configMapper).insert(argThat(config ->
                "encrypted:sk-test-key-12345".equals(config.getApiKey())
        ));
    }

    @Test
    void saveConfig_shouldSucceed_whenNonVipSavesDefault() {
        AiConfigRequest request = new AiConfigRequest();
        request.setProvider("DEFAULT");

        when(configMapper.findByUserId(1L)).thenReturn(null);

        userAiConfigService.saveConfig(1L, "USER", request);

        verify(configMapper).insert(argThat(config ->
                "DEFAULT".equals(config.getProvider()) && config.getApiKey() == null
        ));
    }
}
