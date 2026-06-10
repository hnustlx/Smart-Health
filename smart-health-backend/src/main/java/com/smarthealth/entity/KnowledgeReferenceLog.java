package com.smarthealth.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeReferenceLog {

    private Long id;
    private String knowledgeId;
    private String knowledgeTitle;
    private String knowledgeCategory;
    private String sourceType;
    private Long userId;
    private LocalDateTime createTime;
}
