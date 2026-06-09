package com.smarthealth.controller;

import com.smarthealth.common.Result;
import com.smarthealth.dto.response.CheckinStatusResponse;
import com.smarthealth.security.UserPrincipal;
import com.smarthealth.service.CheckinService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "健康打卡", description = "用户手动打卡和奖励")
@RestController
@RequestMapping("/api/v1/checkin")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @Operation(summary = "查询打卡状态")
    @GetMapping("/status")
    public Result<CheckinStatusResponse> getStatus(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success(checkinService.getStatus(principal.getUserId()));
    }

    @Operation(summary = "今日打卡")
    @PostMapping("/today")
    public Result<CheckinStatusResponse> checkin(@AuthenticationPrincipal UserPrincipal principal) {
        return Result.success("打卡成功", checkinService.checkin(principal.getUserId()));
    }
}
