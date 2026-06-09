package com.smarthealth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class CheckinStatusResponse {
    private List<LocalDate> dates;
    private Integer totalDays;
    private Integer currentStreak;
    private Boolean checkedToday;
    private Integer nextRewardRemainingDays;
    private String rewardCode;
}
