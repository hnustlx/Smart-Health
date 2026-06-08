package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @JsonProperty("token")
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @JsonProperty("userId")
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    @JsonProperty("username")
    @Schema(description = "用户名", example = "testuser")
    private String username;

    @JsonProperty("role")
    @Schema(description = "角色", example = "USER")
    private String role;
}
