package com.smarthealth.service;

import com.smarthealth.entity.KnowledgeReferenceLog;
import com.smarthealth.mapper.KnowledgeReferenceLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeReferenceService {

    private final KnowledgeReferenceLogMapper referenceLogMapper;

    public void recordReferences(List<Map<String, Object>> ragResults, Long userId, String sourceType) {
        if (ragResults == null || ragResults.isEmpty()) {
            return;
        }
        for (Map<String, Object> result : ragResults) {
            Object id = result.get("id");
            if (id == null) {
                continue;
            }
            Map<String, Object> metadata = (Map<String, Object>) result.get("metadata");
            KnowledgeReferenceLog log = new KnowledgeReferenceLog();
            log.setKnowledgeId(String.valueOf(id));
            log.setKnowledgeTitle(metadata != null ? (String) metadata.get("title") : null);
            log.setKnowledgeCategory(metadata != null ? (String) metadata.get("category") : null);
            log.setSourceType(sourceType);
            log.setUserId(userId);
            referenceLogMapper.insert(log);
        }
    }
}
