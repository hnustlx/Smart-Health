package com.smarthealth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTopKnowledgeResponse {

    @JsonProperty("knowledgeId")
    private String knowledgeId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("category")
    private String category;

    @JsonProperty("referenceCount")
    private Long referenceCount;

    @JsonProperty("lastReferencedTime")
    private LocalDateTime lastReferencedTime;
}
