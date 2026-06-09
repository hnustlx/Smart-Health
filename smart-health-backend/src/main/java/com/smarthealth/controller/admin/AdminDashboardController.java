package com.smarthealth.controller.admin;

import com.smarthealth.common.Result;
import com.smarthealth.dto.response.AdminDashboardResponse;
import com.smarthealth.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员后台总览", description = "管理员后台运营总览")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @Operation(summary = "查询后台总览")
    @GetMapping
    public Result<AdminDashboardResponse> getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
