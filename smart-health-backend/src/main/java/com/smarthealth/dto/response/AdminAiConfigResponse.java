package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "管理员视角的 AI 配置（Key 完全隐藏）")
public class AdminAiConfigResponse {

    @JsonProperty("userId")
    @Schema(description = "用户 ID")
    private Long userId;

    @JsonProperty("username")
    @Schema(description = "用户名")
    private String username;

    @JsonProperty("provider")
    @Schema(description = "AI 提供方", example = "CUSTOM")
    private String provider;

    @JsonProperty("customProvider")
    @Schema(description = "自定义 AI 厂商", example = "deepseek")
    private String customProvider;

    @JsonProperty("apiKey")
    @Schema(description = "API Key（管理员不可见）", example = "****")
    private String apiKey;

    @JsonProperty("apiUrl")
    @Schema(description = "API 地址", example = "https://api.deepseek.com")
    private String apiUrl;

    @JsonProperty("model")
    @Schema(description = "模型名称", example = "deepseek-chat")
    private String model;

    @JsonProperty("createdAt")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
