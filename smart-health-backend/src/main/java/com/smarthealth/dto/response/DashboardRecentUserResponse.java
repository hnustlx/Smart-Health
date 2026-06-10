package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardRecentUserResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("role")
    private String role;

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("online")
    private Boolean online;

    @JsonProperty("lastActiveTime")
    private LocalDateTime lastActiveTime;
}
