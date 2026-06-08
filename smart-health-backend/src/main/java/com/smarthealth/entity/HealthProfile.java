package com.smarthealth.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HealthProfile {

    private Long id;
    private Long userId;
    private Integer age;
    private String gender;
    private BigDecimal height;
    private BigDecimal weight;
    private String activityLevel;
    private String dietPreference;
    private String goal;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
