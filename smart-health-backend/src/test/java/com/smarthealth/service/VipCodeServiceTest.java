package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.entity.User;
import com.smarthealth.entity.VipActivationCode;
import com.smarthealth.mapper.UserMapper;
import com.smarthealth.mapper.VipCodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class VipCodeServiceTest {

    @Mock
    private VipCodeMapper vipCodeMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private VipCodeService vipCodeService;

    private VipActivationCode validCode;
    private VipActivationCode usedCode;
    private VipActivationCode expiredCode;
    private User user;

    @BeforeEach
    void setUp() {
        validCode = new VipActivationCode();
        validCode.setId(1L);
        validCode.setCode("VIP-ABCD1234");
        validCode.setStatus(0);
        validCode.setCreatedBy(1L);
        validCode.setCreatedAt(LocalDateTime.now().minusDays(1));
        validCode.setExpiresAt(LocalDateTime.now().plusDays(6));

        usedCode = new VipActivationCode();
        usedCode.setId(2L);
        usedCode.setCode("VIP-USED5678");
        usedCode.setStatus(1);
        usedCode.setUsedBy(2L);
        usedCode.setUsedAt(LocalDateTime.now().minusDays(1));

        expiredCode = new VipActivationCode();
        expiredCode.setId(3L);
        expiredCode.setCode("VIP-EXPIRED90");
        expiredCode.setStatus(0);
        expiredCode.setCreatedAt(LocalDateTime.now().minusDays(10));
        expiredCode.setExpiresAt(LocalDateTime.now().minusDays(3));

        user = new User();
        user.setId(3L);
        user.setUsername("testuser");
        user.setRole("USER");
        user.setStatus(1);
    }

    @Test
    void generateCodes_shouldInsertAndReturnCodes() {
        List<String> codes = vipCodeService.generateCodes(3, 1L);

        assertEquals(3, codes.size());
        for (String code : codes) {
            assertTrue(code.startsWith("VIP-"));
        }
        verify(vipCodeMapper, times(3)).insert(any(VipActivationCode.class));
    }

    @Test
    void activateVip_shouldSucceed() {
        when(vipCodeMapper.findByCode("VIP-ABCD1234")).thenReturn(validCode);
        when(userMapper.findById(3L)).thenReturn(user);
        when(vipCodeMapper.atomicActivate(eq(1L), eq(3L), any(LocalDateTime.class))).thenReturn(1);

        User result = vipCodeService.activateVip("VIP-ABCD1234", 3L);

        assertEquals("VIP", result.getRole());
        assertNotNull(result.getVipExpireTime());
        verify(vipCodeMapper).atomicActivate(eq(1L), eq(3L), any(LocalDateTime.class));
        verify(userMapper).updateRoleAndVipExpire(eq(3L), eq("VIP"), any(LocalDateTime.class));
    }

    @Test
    void activateVip_shouldThrow_whenCodeNotFound() {
        when(vipCodeMapper.findByCode("INVALID")).thenReturn(null);

        assertThrows(BusinessException.class, () -> vipCodeService.activateVip("INVALID", 3L));
    }

    @Test
    void activateVip_shouldThrow_whenCodeUsed() {
        when(vipCodeMapper.findByCode("VIP-USED5678")).thenReturn(usedCode);

        assertThrows(BusinessException.class, () -> vipCodeService.activateVip("VIP-USED5678", 3L));
    }

    @Test
    void activateVip_shouldThrow_whenCodeExpired() {
        when(vipCodeMapper.findByCode("VIP-EXPIRED90")).thenReturn(expiredCode);

        assertThrows(BusinessException.class, () -> vipCodeService.activateVip("VIP-EXPIRED90", 3L));
        verify(vipCodeMapper).updateStatus(eq(3L), eq(2), isNull(), isNull());
    }

    @Test
    void listAll_shouldReturnAllCodes() {
        when(vipCodeMapper.findAll()).thenReturn(List.of(validCode, usedCode, expiredCode));

        List<VipActivationCode> result = vipCodeService.listAll();

        assertEquals(3, result.size());
    }
}
