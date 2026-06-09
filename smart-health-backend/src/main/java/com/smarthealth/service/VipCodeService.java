package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.common.ResultCode;
import com.smarthealth.entity.User;
import com.smarthealth.entity.VipActivationCode;
import com.smarthealth.mapper.UserMapper;
import com.smarthealth.mapper.VipCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VipCodeService {

    private final VipCodeMapper vipCodeMapper;
    private final UserMapper userMapper;

    @Transactional
    public List<String> generateCodes(int count, Long adminId) {
        List<String> codes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(7);

        for (int i = 0; i < count; i++) {
            String code;
            do {
                code = "VIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            } while (vipCodeMapper.findByCode(code) != null);

            VipActivationCode entity = new VipActivationCode();
            entity.setCode(code);
            entity.setCreatedBy(adminId);
            entity.setCreatedAt(now);
            entity.setExpiresAt(expiresAt);
            vipCodeMapper.insert(entity);
            codes.add(code);
        }
        return codes;
    }

    @Transactional
    public User activateVip(String code, Long userId) {
        VipActivationCode record = vipCodeMapper.findByCode(code);
        if (record == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "激活码无效");
        }
        if (record.getStatus() == 1) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "激活码已被使用");
        }
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            vipCodeMapper.updateStatus(record.getId(), 2, null, null);
            throw new BusinessException(ResultCode.BAD_REQUEST, "激活码已过期");
        }

        User user = userMapper.findById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }

        // Atomic update: only one thread can set status from 0 to 1
        int affected = vipCodeMapper.atomicActivate(record.getId(), userId, LocalDateTime.now());
        if (affected == 0) {
            throw new BusinessException(ResultCode.CONFLICT, "激活码已被使用");
        }

        LocalDateTime vipExpireTime = LocalDateTime.now().plusDays(30);
        userMapper.updateRoleAndVipExpire(userId, "VIP", vipExpireTime);

        user.setRole("VIP");
        user.setVipExpireTime(vipExpireTime);
        return user;
    }

    public List<VipActivationCode> listAll() {
        return vipCodeMapper.findAll();
    }
}
