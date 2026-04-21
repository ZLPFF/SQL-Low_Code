package com.sunwayworld.cloud.module.lcdp.agent.service;

import com.sunwayworld.cloud.module.lcdp.agent.config.AgentConfig;
import com.sunwayworld.cloud.module.lcdp.agent.dto.OllamaRequest;
import com.sunwayworld.cloud.module.lcdp.agent.dto.OllamaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OllamaService {

    private final RestTemplate ollamaRestTemplate;
    private final AgentConfig agentConfig;

    public String chat(String systemPrompt, String userMessage) {
        OllamaRequest request = new OllamaRequest();
        request.setModel(agentConfig.getModel());
        request.setStream(false);

        OllamaRequest.Options options = new OllamaRequest.Options();
        options.setNum_predict(agentConfig.getMaxTokens());
        options.setTemperature(agentConfig.getTemperature());
        request.setOptions(options);

        List<Map<String, String>> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
        }
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        request.setMessages(messages);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OllamaRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<OllamaResponse> response = ollamaRestTemplate.postForEntity(
                    agentConfig.getOllamaBaseUrl() + "/api/chat",
                    entity,
                    OllamaResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getResponse();
            }
        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Ollama API: " + e.getMessage(), e);
        }

        return "";
    }

    public String chatWithHistory(List<Map<String, String>> conversationHistory) {
        OllamaRequest request = new OllamaRequest();
        request.setModel(agentConfig.getModel());
        request.setStream(false);

        OllamaRequest.Options options = new OllamaRequest.Options();
        options.setNum_predict(agentConfig.getMaxTokens());
        options.setTemperature(agentConfig.getTemperature());
        request.setOptions(options);
        request.setMessages(conversationHistory);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OllamaRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<OllamaResponse> response = ollamaRestTemplate.postForEntity(
                    agentConfig.getOllamaBaseUrl() + "/api/chat",
                    entity,
                    OllamaResponse.class
            );

            if (response.getBody() != null) {
                return response.getBody().getResponse();
            }
        } catch (Exception e) {
            log.error("Ollama API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call Ollama API: " + e.getMessage(), e);
        }

        return "";
    }

    public boolean isOllamaAvailable() {
        try {
            ollamaRestTemplate.getForObject(agentConfig.getOllamaBaseUrl() + "/api/tags", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Ollama service is not available: {}", e.getMessage());
            return false;
        }
    }

    public List<String> listModels() {
        try {
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to list models: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
