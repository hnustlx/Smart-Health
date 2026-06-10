package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.dto.response.CheckinStatusResponse;
import com.smarthealth.entity.DailyCheckin;
import com.smarthealth.mapper.DailyCheckinMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private DailyCheckinMapper dailyCheckinMapper;

    @Mock
    private VipCodeService vipCodeService;

    @InjectMocks
    private CheckinService checkinService;

    @Test
    void checkin_shouldGenerateRewardCode_whenTotalDaysReaches30() {
        when(dailyCheckinMapper.countByUserIdAndDate(eq(3L), any(LocalDate.class))).thenReturn(0, 1);
        when(dailyCheckinMapper.countByUserId(3L)).thenReturn(30);
        when(dailyCheckinMapper.findRecentByUserId(3L, 365)).thenReturn(createRecords(30));
        when(vipCodeService.generateCheckinRewardCode(3L, 7)).thenReturn("VIP-REWARD7");

        CheckinStatusResponse result = checkinService.checkin(3L);

        assertEquals("VIP-REWARD7", result.getRewardCode());
        assertEquals(30, result.getTotalDays());
        assertTrue(result.getCheckedToday());
        verify(dailyCheckinMapper).insert(eq(3L), any(LocalDate.class));
    }

    @Test
    void checkin_shouldThrow_whenAlreadyCheckedToday() {
        when(dailyCheckinMapper.countByUserIdAndDate(eq(3L), any(LocalDate.class))).thenReturn(1);

        assertThrows(BusinessException.class, () -> checkinService.checkin(3L));
        verify(dailyCheckinMapper, never()).insert(anyLong(), any(LocalDate.class));
        verify(vipCodeService, never()).generateCheckinRewardCode(anyLong(), anyInt());
    }

    private List<DailyCheckin> createRecords(int count) {
        List<DailyCheckin> records = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(count - 1L);
        for (int i = 0; i < count; i++) {
            DailyCheckin record = new DailyCheckin();
            record.setId((long) i + 1);
            record.setUserId(3L);
            record.setCheckinDate(start.plusDays(i));
            records.add(record);
        }
        return records;
    }
}
