package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClaudeAiService {

    private static final Logger log = LoggerFactory.getLogger(ClaudeAiService.class);

    private final RestTemplate restTemplate;

    public String chat(String apiKey, String apiUrl, String model,
                       String systemPrompt, String userPrompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        String effectiveModel = model != null ? model : "claude-3-haiku-20240307";

        Map<String, Object> requestBody = Map.of(
                "model", effectiveModel,
                "max_tokens", 1024,
                "system", systemPrompt,
                "messages", List.of(
                        Map.of("role", "user", "content", userPrompt)
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    apiUrl, request, Map.class);
            Map body = response.getBody();
            if (body == null || !body.containsKey("content")) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Claude 返回格式异常");
            }
            List<Map<String, Object>> contentList = (List<Map<String, Object>>) body.get("content");
            if (contentList == null || contentList.isEmpty()) {
                throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "Claude 返回空结果");
            }
            return (String) contentList.get(0).get("text");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude 服务调用失败: apiUrl={}", apiUrl, e);
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR, "AI 服务异常，请稍后重试");
        }
    }
}
