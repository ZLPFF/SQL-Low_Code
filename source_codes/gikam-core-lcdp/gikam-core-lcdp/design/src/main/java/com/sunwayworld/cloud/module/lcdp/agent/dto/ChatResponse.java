package com.sunwayworld.cloud.module.lcdp.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String sessionId;
    private String message;
    private String status;
    private ActionResult action;
    private List<ChatMessage> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionResult {
        private String actionType;
        private boolean success;
        private String resourceId;
        private String resourceName;
        private Object details;
    }
}
