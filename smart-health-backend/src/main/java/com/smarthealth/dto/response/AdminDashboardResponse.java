package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminDashboardResponse {

    @JsonProperty("users")
    private UserStats users;

    @JsonProperty("plans")
    private PlanStats plans;

    @JsonProperty("knowledge")
    private KnowledgeStats knowledge;

    @JsonProperty("topKnowledge")
    private List<DashboardTopKnowledgeResponse> topKnowledge;

    @JsonProperty("recentActiveUsers")
    private List<DashboardRecentUserResponse> recentActiveUsers;

    @Data
    @AllArgsConstructor
    public static class UserStats {
        private Long total;
        private Long online;
        private Long activeToday;
        private Long active7Days;
        private Long newToday;
        private Long vipTotal;
        private Long disabledTotal;
    }

    @Data
    @AllArgsConstructor
    public static class PlanStats {
        private Long generatedToday;
        private Long generated7Days;
        private Long basicGeneratedToday;
        private Long vipGeneratedToday;
    }

    @Data
    @AllArgsConstructor
    public static class KnowledgeStats {
        private Long total;
        private Long enabled;
        private Long disabled;
        private Long referencedToday;
        private Long referenced7Days;
    }
}
