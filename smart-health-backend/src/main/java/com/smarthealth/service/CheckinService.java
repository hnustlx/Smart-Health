package com.smarthealth.service;

import com.smarthealth.common.BusinessException;
import com.smarthealth.common.ResultCode;
import com.smarthealth.dto.response.CheckinStatusResponse;
import com.smarthealth.entity.DailyCheckin;
import com.smarthealth.mapper.DailyCheckinMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private static final int REWARD_INTERVAL_DAYS = 30;
    private static final int REWARD_VIP_DAYS = 7;

    private final DailyCheckinMapper dailyCheckinMapper;
    private final VipCodeService vipCodeService;

    public CheckinStatusResponse getStatus(Long userId) {
        return buildStatus(userId, null);
    }

    @Transactional
    public CheckinStatusResponse checkin(Long userId) {
        LocalDate today = LocalDate.now();
        if (dailyCheckinMapper.countByUserIdAndDate(userId, today) > 0) {
            throw new BusinessException(ResultCode.CONFLICT, "今天已经打卡");
        }

        dailyCheckinMapper.insert(userId, today);
        int totalDays = dailyCheckinMapper.countByUserId(userId);
        String rewardCode = null;
        if (totalDays % REWARD_INTERVAL_DAYS == 0) {
            rewardCode = vipCodeService.generateCheckinRewardCode(userId, REWARD_VIP_DAYS);
            vipCodeService.activateVip(rewardCode, userId);
        }
        return buildStatus(userId, rewardCode);
    }

    private CheckinStatusResponse buildStatus(Long userId, String rewardCode) {
        List<DailyCheckin> records = dailyCheckinMapper.findRecentByUserId(userId, 365);
        List<LocalDate> dates = records.stream()
                .map(DailyCheckin::getCheckinDate)
                .sorted()
                .toList();
        int totalDays = dailyCheckinMapper.countByUserId(userId);
        int currentStreak = countCurrentStreak(records);
        boolean checkedToday = dailyCheckinMapper.countByUserIdAndDate(userId, LocalDate.now()) > 0;
        int remainingDays = REWARD_INTERVAL_DAYS - (totalDays % REWARD_INTERVAL_DAYS);
        return new CheckinStatusResponse(dates, totalDays, currentStreak, checkedToday, remainingDays, rewardCode);
    }

    private int countCurrentStreak(List<DailyCheckin> records) {
        Set<LocalDate> dates = new HashSet<>();
        for (DailyCheckin record : records) {
            dates.add(record.getCheckinDate());
        }

        int streak = 0;
        LocalDate day = LocalDate.now();
        while (dates.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }
        return streak;
    }
}
