package com.smarthealth.service;

import com.smarthealth.dto.response.AdminDashboardResponse;
import com.smarthealth.dto.response.DashboardRecentUserResponse;
import com.smarthealth.mapper.KnowledgeReferenceLogMapper;
import com.smarthealth.mapper.PlanMapper;
import com.smarthealth.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final int ONLINE_MINUTES = 5;

    private final UserMapper userMapper;
    private final PlanMapper planMapper;
    private final RagService ragService;
    private final KnowledgeReferenceLogMapper referenceLogMapper;

    public AdminDashboardResponse getDashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDaysStart = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime onlineStart = LocalDateTime.now().minusMinutes(ONLINE_MINUTES);

        AdminDashboardResponse.UserStats users = new AdminDashboardResponse.UserStats(
                userMapper.countAll(),
                userMapper.countActiveSince(onlineStart),
                userMapper.countActiveSince(todayStart),
                userMapper.countActiveSince(sevenDaysStart),
                userMapper.countCreatedSince(todayStart),
                userMapper.countByRole("VIP"),
                userMapper.countByStatus(0));

        AdminDashboardResponse.PlanStats plans = new AdminDashboardResponse.PlanStats(
                planMapper.countCreatedSince(todayStart),
                planMapper.countCreatedSince(sevenDaysStart),
                planMapper.countByLevelSince("BASIC", todayStart),
                planMapper.countByLevelSince("VIP", todayStart));

        List<Map<String, Object>> knowledgeItems;
        try {
            knowledgeItems = ragService.listKnowledge(null, null, null, null);
        } catch (Exception e) {
            knowledgeItems = List.of();
        }
        long enabled = 0;
        long disabled = 0;
        for (Map<String, Object> item : knowledgeItems) {
            Map<String, Object> metadata = (Map<String, Object>) item.get("metadata");
            String status = metadata != null ? (String) metadata.get("status") : null;
            if ("disabled".equals(status)) {
                disabled++;
            } else if ("enabled".equals(status)) {
                enabled++;
            }
        }

        AdminDashboardResponse.KnowledgeStats knowledge = new AdminDashboardResponse.KnowledgeStats(
                (long) knowledgeItems.size(),
                enabled,
                disabled,
                referenceLogMapper.countSince(todayStart),
                referenceLogMapper.countSince(sevenDaysStart));

        List<DashboardRecentUserResponse> recentUsers = userMapper.findRecentActive(onlineStart, 8);

        return new AdminDashboardResponse(
                users,
                plans,
                knowledge,
                referenceLogMapper.findTopKnowledge(sevenDaysStart, 8),
                recentUsers);
    }
}
