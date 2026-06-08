package com.smarthealth.controller.admin;

import com.smarthealth.common.Result;
import com.smarthealth.entity.VipActivationCode;
import com.smarthealth.security.UserPrincipal;
import com.smarthealth.service.VipCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "管理员VIP码管理", description = "管理员生成和管理VIP激活码")
@RestController
@RequestMapping("/api/v1/admin/vip-codes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminVipCodeController {

    private final VipCodeService vipCodeService;

    @Operation(summary = "生成VIP激活码")
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateCodes(@RequestParam(defaultValue = "1") int count,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        List<String> codes = vipCodeService.generateCodes(count, principal.getUserId());
        return Result.success(Map.of("codes", codes, "count", codes.size()));
    }

    @Operation(summary = "查看激活码列表")
    @GetMapping("/list")
    public Result<List<VipActivationCode>> listCodes() {
        return Result.success(vipCodeService.listAll());
    }
}
