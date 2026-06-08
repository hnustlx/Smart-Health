package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.common.ResultCode;
import com.smarthealth.dto.request.LoginRequest;
import com.smarthealth.dto.request.RegisterRequest;
import com.smarthealth.dto.response.LoginResponse;
import com.smarthealth.dto.response.UserResponse;
import com.smarthealth.entity.User;
import com.smarthealth.mapper.UserMapper;
import com.smarthealth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void register(RegisterRequest request) {
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException(ResultCode.CONFLICT, "用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }

    public UserResponse getCurrentUser(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        String vipExpireTime = user.getVipExpireTime() != null
                ? user.getVipExpireTime().toString()
                : null;
        return new UserResponse(user.getId(), user.getUsername(), user.getRole(),
                user.getStatus(), vipExpireTime);
    }
}
