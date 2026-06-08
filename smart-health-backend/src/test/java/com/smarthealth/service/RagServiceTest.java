package com.smarthealth.service;

import com.smarthealth.config.ChromaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagServiceTest {

    @Mock
    private ChromaConfig chromaConfig;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RagService ragService;

    @BeforeEach
    void setUp() {
        when(chromaConfig.getUrl()).thenReturn("http://localhost:8000");
        when(chromaConfig.getCollectionName()).thenReturn("test_collection");
        when(restTemplate.getForEntity(contains("/api/v1/collections"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(
                        Map.of("name", "test_collection", "id", "test-uuid-123")
                )));
    }

    @Test
    void addKnowledge_shouldReturnId() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        String id = ragService.addKnowledge("test document", Map.of("title", "Test"));
        assertNotNull(id);
        assertTrue(id.startsWith("knowledge_"));
    }

    @Test
    void addKnowledge_shouldThrow_whenChromaFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> ragService.addKnowledge("test", Map.of()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void query_shouldReturnResults() {
        Map<String, Object> responseBody = Map.of(
                "ids", List.of("id1"),
                "documents", List.of("document1"),
                "metadatas", List.of(Map.of("title", "Test"))
        );
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<Map<String, Object>> results = ragService.query("health tips", 5, "VIP");

        assertEquals(1, results.size());
        assertEquals("id1", results.get(0).get("id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void query_shouldFilterForVIP() {
        Map<String, Object> responseBody = Map.of(
                "ids", List.of("vip-id"),
                "documents", List.of("vip document"),
                "metadatas", List.of(Map.of("level", "vip"))
        );
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<Map<String, Object>> results = ragService.query("health", 5, "VIP");
        assertEquals(1, results.size());
        assertEquals("vip-id", results.get(0).get("id"));
    }

    @Test
    void query_shouldReturnEmpty_whenNoResults() {
        Map<String, Object> responseBody = Map.of(
                "ids", List.of(),
                "documents", List.of(),
                "metadatas", List.of()
        );
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<Map<String, Object>> results = ragService.query("health tips", 5, "VIP");

        assertTrue(results.isEmpty());
    }

    @Test
    void query_shouldThrow_whenChromaFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(com.smarthealth.common.BusinessException.class,
                () -> ragService.query("health tips", 5, "VIP"));
    }

    @Test
    void listKnowledge_shouldFilterByCategory() {
        Map<String, Object> responseBody = Map.of(
                "ids", List.of("id1"),
                "documents", List.of("doc1"),
                "metadatas", List.of(Map.of("category", "饮食"))
        );
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<Map<String, Object>> results = ragService.listKnowledge("饮食", null, null, null);

        assertEquals(1, results.size());
    }

    @Test
    void ensureCollectionExists_shouldCreate_whenNotFound() {
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Not found"));
        when(restTemplate.postForEntity(contains("/collections"), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("name", "test_collection")));

        ragService.ensureCollectionExists();

        verify(restTemplate).postForEntity(contains("/collections"), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void count_shouldReturnZero_whenChromaFails() {
        when(restTemplate.getForEntity(anyString(), eq(Long.class)))
                .thenThrow(new RuntimeException("Error"));

        long count = ragService.count();

        assertEquals(0, count);
    }
}
