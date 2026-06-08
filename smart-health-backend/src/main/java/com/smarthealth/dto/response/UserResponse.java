package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "用户信息响应")
public class UserResponse {

    @JsonProperty("userId")
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @JsonProperty("username")
    @Schema(description = "用户名", example = "testuser")
    private String username;

    @JsonProperty("role")
    @Schema(description = "角色", example = "USER")
    private String role;

    @JsonProperty("status")
    @Schema(description = "状态: 1正常 0禁用", example = "1")
    private Integer status;

    @JsonProperty("vipExpireTime")
    @Schema(description = "VIP 到期时间", example = "2026-12-31T23:59:59")
    private String vipExpireTime;
}
