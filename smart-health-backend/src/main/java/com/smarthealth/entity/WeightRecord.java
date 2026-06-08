package com.smarthealth.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WeightRecord {

    private Long id;
    private Long userId;
    private BigDecimal weight;
    private LocalDate recordDate;
    private LocalDateTime createTime;
}
