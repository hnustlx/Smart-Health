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

    private static final int DEFAULT_VIP_DAYS = 30;

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

            vipCodeMapper.insert(buildCode(code, adminId, DEFAULT_VIP_DAYS, "ADMIN", now, expiresAt));
            codes.add(code);
        }
        return codes;
    }

    @Transactional
    public String generateCheckinRewardCode(Long userId, int vipDays) {
        LocalDateTime now = LocalDateTime.now();
        String code;
        do {
            code = "VIP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (vipCodeMapper.findByCode(code) != null);

        vipCodeMapper.insert(buildCode(code, userId, vipDays, "CHECKIN_REWARD", now, now.plusDays(7)));
        return code;
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
        int vipDays = record.getVipDays() == null ? DEFAULT_VIP_DAYS : record.getVipDays();
        LocalDateTime baseExpireTime = user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now())
                ? user.getVipExpireTime()
                : LocalDateTime.now();
        user.setRole("VIP");
        user.setVipExpireTime(baseExpireTime.plusDays(vipDays));
        userMapper.updateRoleAndVipExpire(userId, "VIP", user.getVipExpireTime());

        vipCodeMapper.updateStatus(record.getId(), 1, userId, LocalDateTime.now());
        return user;
    }

    public List<VipActivationCode> listAll() {
        return vipCodeMapper.findAll();
    }

    private VipActivationCode buildCode(String code, Long createdBy, int vipDays, String source,
                                        LocalDateTime createdAt, LocalDateTime expiresAt) {
        VipActivationCode entity = new VipActivationCode();
        entity.setCode(code);
        entity.setCreatedBy(createdBy);
        entity.setVipDays(vipDays);
        entity.setSource(source);
        entity.setCreatedAt(createdAt);
        entity.setExpiresAt(expiresAt);
        return entity;
    }
}
