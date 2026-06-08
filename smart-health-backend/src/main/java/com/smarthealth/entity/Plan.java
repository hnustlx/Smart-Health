package com.smarthealth.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Plan {

    private Long id;
    private Long userId;
    private String planType;
    private String planLevel;
    private String planContent;
    private String trendSummary;
    private LocalDateTime createTime;
}
