package com.smarthealth.mapper;

import com.smarthealth.dto.response.DashboardTopKnowledgeResponse;
import com.smarthealth.entity.KnowledgeReferenceLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface KnowledgeReferenceLogMapper {

    int insert(KnowledgeReferenceLog log);

    long countSince(@Param("startTime") LocalDateTime startTime);

    List<DashboardTopKnowledgeResponse> findTopKnowledge(@Param("startTime") LocalDateTime startTime,
                                                         @Param("limit") int limit);
}
