package com.smarthealth.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyCheckin {
    private Long id;
    private Long userId;
    private LocalDate checkinDate;
    private LocalDateTime createTime;
}
