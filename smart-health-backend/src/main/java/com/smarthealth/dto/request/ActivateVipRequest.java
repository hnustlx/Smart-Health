package com.smarthealth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "VIP激活请求")
public class ActivateVipRequest {

    @NotBlank(message = "激活码不能为空")
    @Schema(description = "VIP激活码", example = "VIP-1A2B3C4D")
    private String code;
}
