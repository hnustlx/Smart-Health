package com.smarthealth.controller.admin;

import com.smarthealth.common.BusinessException;
import com.smarthealth.common.Result;
import com.smarthealth.common.ResultCode;
import com.smarthealth.dto.response.AdminAiConfigResponse;
import com.smarthealth.entity.UserAiConfig;
import com.smarthealth.mapper.UserAiConfigMapper;
import com.smarthealth.service.UserAiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "管理员 AI 配置管理", description = "管理员查看和重置用户 AI 配置")
@RestController
@RequestMapping("/api/v1/admin/ai-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAiConfigController {

    private final UserAiConfigMapper configMapper;
    private final UserAiConfigService userAiConfigService;

    @Operation(summary = "查询所有用户的 AI 配置（分页）")
    @GetMapping
    public Result<Map<String, Object>> listConfigs(@RequestParam(defaultValue = "1") @Min(1) int page,
                                                    @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
                                                    @RequestParam(required = false) String keyword) {
        int offset = (page - 1) * size;
        List<UserAiConfig> configs = configMapper.findAll(keyword, offset, size);
        long total = configMapper.countAll(keyword);
        List<AdminAiConfigResponse> records = configs.stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
        return Result.success(Map.of("total", total, "records", records));
    }

    @Operation(summary = "查看单个用户的 AI 配置")
    @GetMapping("/{userId}")
    public Result<AdminAiConfigResponse> getConfig(@PathVariable Long userId) {
        UserAiConfig config = configMapper.findByUserIdWithUsername(userId);
        if (config == null) {
            return Result.error(404, "该用户暂无 AI 配置");
        }
        return Result.success(toAdminResponse(config));
    }

    @Operation(summary = "重置用户的 AI 配置为默认")
    @DeleteMapping("/{userId}")
    public Result<Void> resetConfig(@PathVariable Long userId) {
        UserAiConfig config = configMapper.findByUserId(userId);
        if (config == null) {
            return Result.error(404, "该用户暂无 AI 配置");
        }
        userAiConfigService.deleteConfig(userId);
        return Result.success("已重置为默认配置", null);
    }

    private AdminAiConfigResponse toAdminResponse(UserAiConfig config) {
        return new AdminAiConfigResponse(
                config.getUserId(),
                config.getUsername(),
                config.getProvider(),
                config.getCustomProvider(),
                "****",
                config.getApiUrl(),
                config.getModel(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}
