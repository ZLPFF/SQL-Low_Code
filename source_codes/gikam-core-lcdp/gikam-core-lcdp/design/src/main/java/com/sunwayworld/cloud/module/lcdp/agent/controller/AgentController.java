package com.sunwayworld.cloud.module.lcdp.agent.controller;

import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatMessage;
import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatRequest;
import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatResponse;
import com.sunwayworld.cloud.module.lcdp.agent.service.AgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/open/core/module/lcdp/agent")
@RequiredArgsConstructor
@Slf4j
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("Received chat request - sessionId: {}, message: {}", 
                request.getSessionId(), request.getMessage());
        
        try {
            ChatResponse response = agentService.processMessage(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Chat processing failed", e);
            return ResponseEntity.internalServerError()
                    .body(ChatResponse.builder()
                            .status("error")
                            .message("处理失败: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        List<ChatMessage> history = agentService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId) {
        agentService.clearSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "LCDP AI Agent",
                "model", "local-ollama"
        ));
    }
}
