package com.smarthealth.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VipActivationCode {
    private Long id;
    private String code;
    private Integer status;
    private Long usedBy;
    private Long createdBy;
    private Integer vipDays;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
}
