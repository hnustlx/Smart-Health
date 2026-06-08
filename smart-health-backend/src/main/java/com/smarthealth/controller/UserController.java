package com.smarthealth.controller;

import com.smarthealth.common.Result;
import com.smarthealth.dto.request.ActivateVipRequest;
import com.smarthealth.dto.request.LoginRequest;
import com.smarthealth.dto.request.RegisterRequest;
import com.smarthealth.dto.response.LoginResponse;
import com.smarthealth.dto.response.UserResponse;
import com.smarthealth.entity.User;
import com.smarthealth.security.UserPrincipal;
import com.smarthealth.service.UserService;
import com.smarthealth.service.VipCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理", description = "用户注册、登录、当前信息查询、VIP激活")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VipCodeService vipCodeService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return Result.success("注册成功", null);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.success("登录成功", response);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public Result<UserResponse> currentUser(@AuthenticationPrincipal UserPrincipal principal) {
        UserResponse response = userService.getCurrentUser(principal.getUserId());
        return Result.success(response);
    }

    @Operation(summary = "VIP激活")
    @PostMapping("/activate-vip")
    public Result<UserResponse> activateVip(@AuthenticationPrincipal UserPrincipal principal,
                                            @Valid @RequestBody ActivateVipRequest request) {
        User user = vipCodeService.activateVip(request.getCode(), principal.getUserId());
        String vipExpireTime = user.getVipExpireTime() != null
                ? user.getVipExpireTime().toString()
                : null;
        return Result.success("VIP激活成功", new UserResponse(
                user.getId(), user.getUsername(), user.getRole(),
                user.getStatus(), vipExpireTime));
    }
}
